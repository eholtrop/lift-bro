package com.lift.bro.data.core.datasource

import com.lift.bro.domain.feed.Post
import com.lift.bro.domain.models.ATProtoCredentials
import kotlinx.coroutines.flow.Flow

interface FeedDataSource {
    suspend fun post(record: PostRecord, credentials: ATProtoCredentials): Result<Post>

    fun getWorkoutPosts(credentials: ATProtoCredentials): Flow<Result<List<Post>>>
}

sealed class PostRecord {
    abstract val text: String

    data class TextOnly(override val text: String) : PostRecord()

    data class WithWorkout(
        override val text: String,
        val workoutJson: String,
    ) : PostRecord()
}
