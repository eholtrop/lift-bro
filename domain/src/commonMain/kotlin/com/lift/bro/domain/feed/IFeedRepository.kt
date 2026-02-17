package com.lift.bro.domain.feed

import com.lift.bro.domain.models.ATProtoCredentials
import com.lift.bro.domain.models.Workout
import kotlinx.coroutines.flow.Flow

interface IFeedRepository {
    suspend fun postWorkout(
        workout: Workout,
        credentials: ATProtoCredentials,
    ): Result<Post>

    suspend fun postText(
        text: String,
        credentials: ATProtoCredentials,
    ): Result<Post>

    fun getWorkoutPosts(credentials: ATProtoCredentials): Flow<Result<List<Post>>>
}
