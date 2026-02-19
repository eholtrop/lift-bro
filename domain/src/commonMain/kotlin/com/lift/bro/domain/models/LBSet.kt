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
    val tempo: Tempo = Tempo(),
    val date: Instant = Clock.System.now(),
    val notes: String = "",
    val rpe: Int? = null,
    val mer: Int = 0,
    val bodyWeightRep: Boolean? = null,
    val videoUri: String? = null,
) {
    val totalWeightMoved = weight * reps
}

fun calculateMax(reps: Long?, weight: Double?) = calculateMax(reps?.toInt() ?: 0, weight ?: 0.0)

fun calculateMax(reps: Int, weight: Double): Double {
    return when (reps) {
        1 -> weight
        else -> estimatedMax(reps, weight)
    }
}

private const val MER_DENOMINATOR = 30.0

fun estimatedMax(reps: Int, weight: Double): Double {
    return weight * (1 + (reps / MER_DENOMINATOR))
}

@Serializable
data class Tempo(
    val down: Long = 3,
    val hold: Long = 1,
    val up: Long = 1,
)

val LBSet.formattedMax: String get() = "${this.reps} x ${this.formattedTempo}"

val LBSet.formattedTempo: String get() = "${this.tempo.down}/${this.tempo.hold}/${this.tempo.up}"

val LBSet.formattedReps: String get() = "${this.formattedTempo} x ${this.reps}"

val LBSet.oneRepMax: Double? get() = if (this.reps == 1L) weight else null

val LBSet.estimateMax: Double? get() = estimatedMax(
    this.reps.toInt(),
    this.weight
)
