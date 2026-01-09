package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

typealias GoalId = String

@Serializable
data class Goal(
    val id: GoalId = uuid4().toString(),
    val name: String,
    val achieved: Boolean = false,
    val createdAt: Instant = Clock.System.now(),
    val updatedAt: Instant = Clock.System.now(),
)
