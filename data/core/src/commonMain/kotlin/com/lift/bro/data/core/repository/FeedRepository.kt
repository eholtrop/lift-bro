package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.FeedDataSource
import com.lift.bro.data.core.datasource.PostRecord
import com.lift.bro.domain.feed.IFeedRepository
import com.lift.bro.domain.feed.Post
import com.lift.bro.domain.models.ATProtoCredentials
import com.lift.bro.domain.models.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class FeedRepository(
    private val feedDataSource: FeedDataSource,
) : IFeedRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        private const val MAX_EXERCISES_IN_PREVIEW = 3
    }

    override suspend fun postWorkout(
        workout: Workout,
        credentials: ATProtoCredentials,
    ): Result<Post> {
        val workoutJson = json.encodeToString(workout)
        val text = formatWorkoutText(workout)

        val record = PostRecord.WithWorkout(
            text = text,
            workoutJson = workoutJson,
        )

        return feedDataSource.post(record, credentials)
    }

    override suspend fun postText(
        text: String,
        credentials: ATProtoCredentials,
    ): Result<Post> {
        val record = PostRecord.TextOnly(text)
        return feedDataSource.post(record, credentials)
    }

    override fun getWorkoutPosts(credentials: ATProtoCredentials): Flow<Result<List<Post>>> {
        return feedDataSource.getWorkoutPosts(credentials)
    }

    private fun formatWorkoutText(workout: Workout): String {
        val exerciseNames = workout.exercises
            .mapNotNull { it.variationSets.firstOrNull()?.variation?.lift?.name }
            .distinct()
            .take(MAX_EXERCISES_IN_PREVIEW)

        return buildString {
            append("💪 Workout Complete! ")
            if (exerciseNames.isNotEmpty()) {
                append(exerciseNames.joinToString(", "))
                if (workout.exercises.size > MAX_EXERCISES_IN_PREVIEW) {
                    append(" +${workout.exercises.size - MAX_EXERCISES_IN_PREVIEW} more")
                }
            }
            append("\n")
            append("#fitness #workout #liftbro")
        }
    }
}
