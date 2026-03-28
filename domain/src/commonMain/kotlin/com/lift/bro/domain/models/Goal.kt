package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import com.lift.bro.domain.serializers.InstantSerializer
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

typealias GoalId = String

@Serializable
data class Goal(
    val id: GoalId = uuid4().toString(),
    val name: String,
    val achieved: Boolean = false,
    @Serializable(with = InstantSerializer::class) val createdAt: Instant = Clock.System.now(),
    @Serializable(with = InstantSerializer::class) val updatedAt: Instant = Clock.System.now(),
)
