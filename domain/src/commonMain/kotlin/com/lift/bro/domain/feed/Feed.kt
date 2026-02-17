package com.lift.bro.domain.feed

import kotlinx.serialization.Serializable

@Serializable
data class Feed(
    val id: String,
    val name: String,
    val uri: String,
    val creatorDid: String,
    val isPublic: Boolean,
    val isOwn: Boolean,
)
