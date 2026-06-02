package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutHistory(
    val summary: HistorySummary,
    val exerciseHistory: List<ExerciseHistoryEntry>,
    val recentWorkouts: List<WorkoutSummary>,
)

@Serializable
data class HistorySummary(
    val totalWorkouts: Int,
    val totalVolume: Double,
    val mostConsistentMuscleGroups: List<MuscleGroup>,
    val leastConsistentMuscleGroups: List<MuscleGroup>,
    val averageWorkoutsPerWeek: Double,
    val currentStreak: Int,
    val personalRecords: List<PersonalRecord>,
)

@Serializable
data class ExerciseHistoryEntry(
    val variationId: String,
    val variationName: String,
    val liftName: String,
    val muscleGroup: MuscleGroup,
    val totalSessions: Int,
    val bestWeight: Double,
    val bestReps: Long,
    val bestEstimatedMax: Double,
    val averageWeight: Double,
    val averageReps: Double,
    val lastPerformed: String,
    val frequencyPerWeek: Double,
)

@Serializable
data class WorkoutSummary(
    val id: String,
    val date: String,
    val muscleGroupsWorked: List<MuscleGroup>,
    val totalExercises: Int,
    val totalVolume: Double,
)

@Serializable
data class PersonalRecord(
    val liftName: String,
    val variationName: String,
    val weight: Double,
    val reps: Long,
    val estimatedMax: Double,
    val date: String,
)

@Serializable
data class ExerciseHistory(
    val variationId: String,
    val sets: List<SetSummary>,
)

@Serializable
data class SetSummary(
    val weight: Double,
    val reps: Long,
    val estimatedMax: Double,
    val date: String,
)

fun emptyWorkoutHistory() = WorkoutHistory(
    summary = HistorySummary(
        totalWorkouts = 0,
        totalVolume = 0.0,
        mostConsistentMuscleGroups = emptyList(),
        leastConsistentMuscleGroups = emptyList(),
        averageWorkoutsPerWeek = 0.0,
        currentStreak = 0,
        personalRecords = emptyList(),
    ),
    exerciseHistory = emptyList(),
    recentWorkouts = emptyList(),
)
