package com.lift.bro.data.atproto

import com.lift.bro.data.core.datasource.FeedDataSource
import com.lift.bro.data.core.datasource.PostRecord
import com.lift.bro.domain.feed.Post
import com.lift.bro.domain.models.ATProtoCredentials
import com.lift.bro.domain.models.Workout
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class ATProtoFeedDataSource: FeedDataSource {

    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun post(
        record: PostRecord,
        credentials: ATProtoCredentials,
    ): Result<Post> {
        return try {
            val postRecord = when (record) {
                is PostRecord.TextOnly -> {
                    mapOf(
                        "\$type" to "app.bsky.feed.post",
                        "text" to record.text,
                        "createdAt" to Clock.System.now().toString(),
                    )
                }

                is PostRecord.WithWorkout -> {
                    val workout = json.decodeFromString<Workout>(record.workoutJson)

                    val workoutRef = createWorkoutRecord(
                        credentials = credentials,
                        workout = workout,
                    )

                    mapOf(
                        "\$type" to "app.bsky.feed.post",
                        "text" to record.text,
                        "createdAt" to Clock.System.now().toString(),
                        "embed" to mapOf(
                            "\$type" to "app.bsky.embed.record",
                            "record" to workoutRef,
                        ),
                    )
                }
            }

            val response: CreateRecordResponse = httpClient.post(
                "${credentials.pdsHost}/xrpc/com.atproto.repo.createRecord"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${credentials.accessJwt}")
                setBody(
                    mapOf(
                        "repo" to credentials.did,
                        "collection" to "app.bsky.feed.post",
                        "record" to postRecord,
                    )
                )
            }.body()

            val workout = when (record) {
                is PostRecord.WithWorkout -> json.decodeFromString<Workout>(record.workoutJson)
                else -> null
            }

            Result.success(
                Post(
                    id = response.uri ?: "",
                    authorDid = credentials.did,
                    authorHandle = credentials.handle,
                    text = record.text,
                    createdAt = Clock.System.now(),
                    workout = workout,
                    uri = response.uri ?: "",
                    cid = response.cid ?: "",
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getWorkoutPosts(credentials: ATProtoCredentials): Flow<Result<List<Post>>> = flow {
        try {
            val responseJson: String = httpClient.get("${credentials.pdsHost}/xrpc/app.bsky.feed.getTimeline") {
                header("Authorization", "Bearer ${credentials.accessJwt}")
                parameter("limit", 100)
                parameter("algorithm", "reverse_chronological")
            }.body()

            val jsonElement = json.parseToJsonElement(responseJson)
            val feed = jsonElement.jsonObject["feed"]?.jsonArray

            val posts = feed?.mapNotNull { item ->
                try {
                    val itemObj = item.jsonObject
                    val postObj = itemObj["post"]?.jsonObject ?: return@mapNotNull null

                    val recordObj = postObj["record"]?.jsonObject
                    val embedObj = recordObj?.get("embed")?.jsonObject

                    val workout = embedObj?.let { parseWorkoutFromEmbed(it) }

                    Post(
                        id = postObj["uri"]?.jsonPrimitive?.content ?: "",
                        authorDid = postObj["author"]?.jsonObject?.get("did")?.jsonPrimitive?.content ?: "",
                        authorHandle = postObj["author"]?.jsonObject?.get("handle")?.jsonPrimitive?.content ?: "",
                        text = recordObj?.get("text")?.jsonPrimitive?.content ?: "",
                        createdAt = parseInstant(postObj["indexedAt"]?.jsonPrimitive?.content),
                        workout = workout,
                        uri = postObj["uri"]?.jsonPrimitive?.content ?: "",
                        cid = postObj["cid"]?.jsonPrimitive?.content ?: "",
                        likeCount = postObj["likeCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        repostCount = postObj["repostCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                        replyCount = postObj["replyCount"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0,
                    )
                } catch (e: Exception) {
                    null
                }
            }?.filter { it.workout != null } ?: emptyList()

            emit(Result.success(posts))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }

    }

    private suspend fun createWorkoutRecord(
        credentials: ATProtoCredentials,
        workout: Workout,
    ): Map<String, String> {
        val workoutJsonString = json.encodeToString(Workout.serializer(), workout)
        val workoutMap: Map<String, Any> = json.decodeFromString(workoutJsonString)

        val workoutRecord = buildMap {
            put("\$type", "app.liftbro.workout")
            put("workout", workoutMap)
            put("createdAt", Clock.System.now().toString())
        }

        val response: CreateRecordResponse = httpClient.post(
            "${credentials.pdsHost}/xrpc/com.atproto.repo.createRecord"
        ) {
            contentType(ContentType.Application.Json)
            header("Authorization", "Bearer ${credentials.accessJwt}")
            setBody(
                mapOf(
                    "repo" to credentials.did,
                    "collection" to "app.liftbro.workout",
                    "record" to workoutRecord,
                )
            )
        }.body()

        return mapOf(
            "uri" to (response.uri ?: ""),
            "cid" to (response.cid ?: ""),
        )
    }

    private fun parseWorkoutFromEmbed(embed: kotlinx.serialization.json.JsonObject): Workout? {
        return try {
            val record = embed["record"]?.jsonObject ?: return null
            val workoutElement = record["workout"] ?: return null

            val workoutJson = workoutElement.toString()
            json.decodeFromString<Workout>(workoutJson)
        } catch (e: Exception) {
            null
        }
    }

    private fun parseInstant(instant: String?): Instant {
        return try {
            instant?.let { Instant.parse(it) } ?: Clock.System.now()
        } catch (e: Exception) {
            Clock.System.now()
        }
    }
}

@Serializable
private data class CreateRecordResponse(
    val uri: String? = null,
    val cid: String? = null,
)
