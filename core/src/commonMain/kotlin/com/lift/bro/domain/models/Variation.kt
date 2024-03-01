package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Variation(
    val id: String,
    val liftId: String,
    val name: String?,
)