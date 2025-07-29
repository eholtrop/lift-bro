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

    val totalWeightMoved =  weight * reps
}

fun calculateMax(reps: Long?, weight: Double?) = calculateMax(reps?.toInt() ?: 0, weight ?: 0.0)

fun calculateMax(reps: Int, weight: Double): Double {
    return when (reps) {
        1 -> weight
        else -> estimatedMax(reps, weight)
    }
}

fun estimatedMax(reps: Int, weight: Double): Double {
    return weight * (1 + (reps / 30.0))
}


@Serializable
data class Tempo(
    val down: Long = 3,
    val hold: Long = 1,
    val up: Long = 1,
)