package com.lift.bro.domain.models

import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class LBSet(
    val id: String,
    val variationId: String,
    val weight: Double = 0.0,
    val reps: Long = 1,
    val tempo: Tempo = Tempo(),
    val date: Instant = Clock.System.now(),
    val notes: String,
    val rpe: Int? = null,
    val mer: Int = 0,
) {
    val excerciseId = variationId + date.toLocalDate().toString("dd-MM-yyyy")
}

@Serializable
data class Tempo(
    val down: Long = 3,
    val hold: Long = 1,
    val up: Long = 1,
)