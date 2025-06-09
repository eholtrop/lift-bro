package com.lift.bro.domain.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Excercise(
    val sets: List<LBSet>,
    val variation: Variation,
    val date: LocalDate,
) {
    val id = sets.firstOrNull()?.excerciseId
}