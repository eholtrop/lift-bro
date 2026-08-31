package com.lift.bro.backup

import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.serializers.InstantSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
data class Backup(
    val lifts: List<Category>? = null,
    val variations: List<Movement>? = null,
    val sets: List<LBSet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
    val workouts: List<Workout>? = null,
    val exercises: List<Exercise>? = null,
)

@Serializable
@JsonIgnoreUnknownKeys
@OptIn(ExperimentalSerializationApi::class)
data class LegacyVariation(
    val id: String,
    val lift: Category? = null,
    val name: String? = null,
    val reps: Long = 1,
    val favourite: Boolean = false,
    val notes: String? = null,
    val bodyWeight: Boolean? = false,
)

@Serializable
data class LegacyVariationSet(
    val id: String,
    val sets: List<LegacySet> = emptyList(),
    val variation: LegacyVariation,
)

@Serializable
data class LegacyExercise(
    val id: String,
    val workoutId: String,
    val variationSets: List<LegacyVariationSet> = emptyList(),
)

@Serializable
data class LegacyWorkout(
    val id: String,
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<LegacyExercise> = emptyList(),
    val finisher: String? = null,
)

@Serializable
data class LegacyBackup(
    val lifts: List<Category>? = null,
    val variations: List<LegacyVariation>? = null,
    val sets: List<LegacySet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
    val workouts: List<LegacyWorkout>? = null,
    val exercises: List<LegacyExercise>? = null,
)

@Serializable
data class LegacySet(
    val id: String,
    val variationId: String,
    val weight: Double = 0.0,
    val reps: Long = 1,
    val tempo: Tempo = Tempo(),
    @Serializable(with = InstantSerializer::class) val date: Instant = Clock.System.now(),
    val notes: String = "",
    val rpe: Int? = null,
    val mer: Int = 0,
    val bodyWeightRep: Boolean? = null,
    val videoUri: String? = null,
) {
    val totalWeightMoved = weight * reps
}
