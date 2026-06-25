package com.lift.bro.domain.models

import com.benasher44.uuid.uuid4
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

typealias WorkoutId = String

@Serializable
data class Workout(
    val id: WorkoutId = uuid4().toString(),
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val finisher: String? = null,
)
