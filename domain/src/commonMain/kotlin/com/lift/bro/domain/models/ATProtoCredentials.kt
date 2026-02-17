package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class ATProtoCredentials(
    val did: String,
    val accessJwt: String,
    val refreshJwt: String,
    val handle: String,
    val pdsHost: String = "https://bsky.social",
)
