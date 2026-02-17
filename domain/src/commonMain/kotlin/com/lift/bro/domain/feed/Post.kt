package com.lift.bro.domain.feed

import com.lift.bro.domain.models.Workout
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: String,
    val authorDid: String,
    val authorHandle: String,
    val text: String,
    val createdAt: Instant,
    val workout: Workout? = null,
    val uri: String,
    val cid: String,
    val likeCount: Int = 0,
    val repostCount: Int = 0,
    val replyCount: Int = 0,
)
