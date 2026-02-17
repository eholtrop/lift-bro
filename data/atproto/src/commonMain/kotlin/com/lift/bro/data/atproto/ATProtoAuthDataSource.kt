package com.lift.bro.data.atproto

import com.lift.bro.data.core.datasource.FeedAuthDataSource
import com.lift.bro.domain.models.ATProtoCredentials
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class ATProtoAuthDataSource : FeedAuthDataSource {

    private val httpClient = HttpClient()
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    override suspend fun createSession(
        handle: String,
        password: String,
    ): Result<ATProtoCredentials> {
        return try {
            val response: SessionResponse = httpClient.post(
                "https://bsky.social/xrpc/com.atproto.server.createSession"
            ) {
                contentType(ContentType.Application.Json)
                setBody(CreateSessionRequest(handle, password))
            }.body()

            Result.success(
                ATProtoCredentials(
                    did = response.did,
                    accessJwt = response.accessJwt,
                    refreshJwt = response.refreshJwt,
                    handle = response.handle,
                    pdsHost = response.pdsUrl ?: "https://bsky.social",
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createSessionOAuth(pdsHost: String): Result<ATProtoCredentials> {
        return Result.failure(
            NotImplementedError("OAuth flow not yet implemented - use password authentication")
        )
    }

    override suspend fun refreshSession(
        credentials: ATProtoCredentials,
    ): Result<ATProtoCredentials> {
        return try {
            val response: SessionResponse = httpClient.post(
                "${credentials.pdsHost}/xrpc/com.atproto.server.refreshSession"
            ) {
                contentType(ContentType.Application.Json)
                header("Authorization", "Bearer ${credentials.refreshJwt}")
            }.body()

            Result.success(
                credentials.copy(
                    accessJwt = response.accessJwt,
                    refreshJwt = response.refreshJwt,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Serializable
private data class CreateSessionRequest(
    val identifier: String,
    val password: String,
)

@Serializable
private data class SessionResponse(
    val did: String,
    val accessJwt: String,
    val refreshJwt: String,
    val handle: String,
    val pdsUrl: String? = null,
)
