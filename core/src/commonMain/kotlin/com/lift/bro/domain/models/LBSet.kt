package com.lift.bro.domain.models

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class LBSet(
    val id: String,
    val variationId: String,
    val weight: Double = 0.0,
    val reps: Long = 1,
    val tempoDown: Long = 3,
    val tempoHold: Long = 1,
    val tempoUp: Long = 1,
    val date: Instant = Clock.System.now(),
)