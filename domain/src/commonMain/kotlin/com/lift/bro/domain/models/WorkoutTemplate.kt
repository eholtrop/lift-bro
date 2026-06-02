package com.lift.bro.domain.models

import kotlinx.serialization.Serializable

@Serializable
data class WorkoutTemplate(
    val dayName: String,
    val focus: MuscleGroup,
    val exercises: List<ExerciseTemplate>,
)

@Serializable
data class ExerciseTemplate(
    val name: String,
    val variationName: String? = null,
    val liftName: String,
    val sets: Int,
    val reps: String,
    val suggestedWeight: Double? = null,
    val notes: String? = null,
)

@Serializable
enum class MuscleGroup {
    Chest,
    Back,
    Shoulders,
    Biceps,
    Triceps,
    Legs,
    Core,
    FullBody,
    Push,
    Pull,
    LegsLower,
    LegsUpper,
}

@Serializable
data class SetRecommendation(
    val variationId: String,
    val suggestedWeight: Double,
    val suggestedReps: Long,
    val confidence: Float,
    val basedOn: String,
)

@Serializable
data class WorkoutPreferences(
    val goal: WorkoutGoal = WorkoutGoal.GeneralFitness,
    val experienceLevel: ExperienceLevel = ExperienceLevel.Intermediate,
    val daysPerWeek: Int = 4,
    val focusMuscleGroups: List<MuscleGroup> = emptyList(),
    val availableEquipment: List<Equipment> = emptyList(),
    val targetMuscleGroups: List<MuscleGroup> = emptyList(),
    val workoutDurationMinutes: Int = 45,
    val exerciseCount: Int = 5,
    val difficulty: Difficulty = Difficulty.Intermediate,
)

@Serializable
enum class Difficulty {
    Beginner,
    Intermediate,
    Advanced,
}

@Serializable
enum class WorkoutGoal {
    Strength,
    Hypertrophy,
    Endurance,
    GeneralFitness,
}

@Serializable
enum class ExperienceLevel {
    Beginner,
    Intermediate,
    Advanced,
}

@Serializable
enum class Equipment {
    Barbell,
    Dumbbell,
    Cable,
    Machine,
    Bodyweight,
    Kettlebell,
    ResistanceBand,
}

fun WorkoutTemplate.toDisplayString(): String {
    return buildString {
        appendLine("=== $dayName - ${focus.name} ===")
        appendLine()
        exercises.forEachIndexed { index, exercise ->
            appendLine("${index + 1}. ${exercise.name}")
            appendLine("   ${exercise.sets} x ${exercise.reps}")
            exercise.suggestedWeight?.let {
                appendLine("   Weight: ${it}lbs")
            }
            exercise.notes?.let {
                appendLine("   Notes: $it")
            }
            appendLine()
        }
    }
}
