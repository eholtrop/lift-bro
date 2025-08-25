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