package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable

@Serializable
data class Lift(
    val id: String = uuid4().toString(),
    val name: String = "",
    val color: ULong? = null,
    val maxWeight: Double? = null,
)