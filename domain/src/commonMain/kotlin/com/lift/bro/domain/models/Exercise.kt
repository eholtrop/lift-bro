package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

typealias ExerciseId = String

@Serializable
data class VariationSets(
    val id: String,
    val variation: Variation,
    val sets: List<LBSet>
)

@Serializable
data class Exercise(
    val id: String,
    val workoutId: String,
    val variationSets: List<VariationSets>,
) {
    val totalWeightMoved = variationSets.sumOf { it.sets.sumOf { it.totalWeightMoved } }
}
