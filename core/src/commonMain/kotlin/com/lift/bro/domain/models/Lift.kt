package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Lift(
    val id: String,
    val name: String,
    val color: ULong? = null,
    val maxWeight: Double? = null,
)