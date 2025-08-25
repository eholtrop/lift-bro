package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class Exercise(
    val sets: List<LBSet>,
    val variation: Variation,
) {
    val id = sets.firstOrNull()?.excerciseId

    val totalWeightMoved = sets.sumOf { it.totalWeightMoved }
}