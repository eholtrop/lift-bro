package com.lift.bro.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Workout(
    val date: LocalDate,
    val warmup: String = "",
    val exercises: List<Exercise>,
    val finisher: String = "",
)

@Serializable
data class Exercise(
    val sets: List<LBSet>,
    val variation: Variation,
) {
    val id = sets.firstOrNull()?.excerciseId

    val totalWeightMoved = sets.sumOf { it.totalWeightMoved }
}