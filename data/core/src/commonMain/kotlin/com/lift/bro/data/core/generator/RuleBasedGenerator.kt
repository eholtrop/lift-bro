package com.lift.bro.data.core.generator

import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.ExerciseTemplate
import com.lift.bro.domain.models.MuscleGroup
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.models.WorkoutGoal
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.WorkoutTemplate
import com.lift.bro.domain.repositories.WorkoutGenerator
import kotlin.math.roundToInt

class RuleBasedGenerator : WorkoutGenerator {

    override suspend fun generateWorkout(
        history: WorkoutHistory,
        preferences: WorkoutPreferences
    ): Result<WorkoutTemplate> {
        return try {
            val template = generateFromRuleBased(history, preferences)
            Result.success(template)
        } catch (e: IllegalStateException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    override suspend fun suggestWeight(
        variationId: String,
        targetReps: Long,
        history: ExerciseHistory
    ): Result<SetRecommendation> {
        return try {
            val bestSet = history.sets.maxByOrNull { it.estimatedMax }
                ?: return Result.failure(IllegalStateException("No history available"))

            val suggestedWeight = calculateWeightForReps(
                bestSet.estimatedMax,
                targetReps
            )

            Result.success(
                SetRecommendation(
                    variationId = variationId,
                    suggestedWeight = suggestedWeight,
                    suggestedReps = targetReps,
                    confidence = 0.7f,
                    basedOn = "Based on your ${bestSet.estimatedMax.roundToInt()}lbs estimated max",
                )
            )
        } catch (e: IllegalStateException) {
            Result.failure(e)
        } catch (e: IllegalArgumentException) {
            Result.failure(e)
        }
    }

    private fun generateFromRuleBased(
        history: WorkoutHistory,
        preferences: WorkoutPreferences
    ): WorkoutTemplate {
        val focus = determineFocus(preferences, history)
        val exercises = generateExercises(focus, preferences.goal, history)

        return WorkoutTemplate(
            dayName = getDayName(focus),
            focus = focus,
            exercises = exercises,
        )
    }

    @Suppress("UNUSED_PARAMETER")
    private fun determineFocus(
        preferences: WorkoutPreferences,
        history: WorkoutHistory
    ): MuscleGroup {
        if (preferences.focusMuscleGroups.isNotEmpty()) {
            return preferences.focusMuscleGroups.first()
        }

        return when (preferences.daysPerWeek) {
            3 -> listOf(MuscleGroup.FullBody, MuscleGroup.FullBody, MuscleGroup.FullBody).random()
            4 -> listOf(MuscleGroup.Push, MuscleGroup.Pull, MuscleGroup.LegsLower, MuscleGroup.LegsUpper).random()
            5 -> listOf(
                MuscleGroup.Chest,
                MuscleGroup.Back,
                MuscleGroup.Shoulders,
                MuscleGroup.Legs,
                MuscleGroup.Triceps
            ).random()
            else -> MuscleGroup.FullBody
        }
    }

    private fun generateExercises(
        focus: MuscleGroup,
        goal: WorkoutGoal,
        history: WorkoutHistory
    ): List<ExerciseTemplate> {
        val exerciseList = when (focus) {
            MuscleGroup.Push -> PushExercises
            MuscleGroup.Pull -> PullExercises
            MuscleGroup.Legs, MuscleGroup.LegsLower, MuscleGroup.LegsUpper -> LegExercises
            MuscleGroup.Chest -> ChestExercises
            MuscleGroup.Back -> BackExercises
            MuscleGroup.Shoulders -> ShoulderExercises
            MuscleGroup.FullBody -> FullBodyExercises
            else -> FullBodyExercises
        }

        val sets = when (goal) {
            WorkoutGoal.Strength -> 5
            WorkoutGoal.Hypertrophy -> 4
            WorkoutGoal.Endurance -> 3
            WorkoutGoal.GeneralFitness -> 3
        }

        val reps = when (goal) {
            WorkoutGoal.Strength -> "3-5"
            WorkoutGoal.Hypertrophy -> "8-12"
            WorkoutGoal.Endurance -> "12-15"
            WorkoutGoal.GeneralFitness -> "8-12"
        }

        val primaryExercises = exerciseList.take(3)
        val secondaryExercises = exerciseList.drop(3).take(2)

        val primaryTemplates = primaryExercises.map { exercise: ExerciseInfo ->
            ExerciseTemplate(
                name = exercise.name,
                variationName = exercise.variation,
                liftName = exercise.lift,
                sets = sets,
                reps = reps,
                suggestedWeight = estimateWeight(exercise, history),
            )
        }

        val secondaryTemplates = secondaryExercises.map { exercise: ExerciseInfo ->
            ExerciseTemplate(
                name = exercise.name,
                variationName = exercise.variation,
                liftName = exercise.lift,
                sets = sets - 1,
                reps = reps,
                suggestedWeight = estimateWeight(exercise, history),
            )
        }

        return primaryTemplates + secondaryTemplates
    }

    private fun estimateWeight(exercise: ExerciseInfo, history: WorkoutHistory): Double? {
        val historyEntry = history.exerciseHistory.find {
            it.liftName.equals(exercise.lift, ignoreCase = true) ||
                it.variationName.equals(exercise.variation, ignoreCase = true)
        }

        return historyEntry?.let { entry ->
            val targetReps = 8
            val estimatedMax = entry.bestEstimatedMax
            calculateWeightForReps(estimatedMax, targetReps.toLong())
        }
    }

    private fun calculateWeightForReps(oneRepMax: Double, targetReps: Long): Double {
        return if (targetReps == 1L) oneRepMax else oneRepMax / (1 + (targetReps / 30.0))
    }

    private fun getDayName(focus: MuscleGroup): String {
        return when (focus) {
            MuscleGroup.Push -> "Push Day"
            MuscleGroup.Pull -> "Pull Day"
            MuscleGroup.Legs, MuscleGroup.LegsLower, MuscleGroup.LegsUpper -> "Leg Day"
            MuscleGroup.Chest -> "Chest Day"
            MuscleGroup.Back -> "Back Day"
            MuscleGroup.Shoulders -> "Shoulder Day"
            MuscleGroup.Biceps -> "Arms - Biceps"
            MuscleGroup.Triceps -> "Arms - Triceps"
            MuscleGroup.Core -> "Core Day"
            MuscleGroup.FullBody -> "Full Body"
        }
    }

    companion object {
        private data class ExerciseInfo(
            val name: String,
            val variation: String?,
            val lift: String,
        )

        private val PushExercises = listOf(
            ExerciseInfo("Bench Press", "Barbell", "Chest"),
            ExerciseInfo("Overhead Press", "Barbell", "Shoulders"),
            ExerciseInfo("Incline Dumbbell Press", null, "Chest"),
            ExerciseInfo("Tricep Pushdown", "Cable", "Triceps"),
            ExerciseInfo("Lateral Raise", null, "Shoulders"),
        )

        private val PullExercises = listOf(
            ExerciseInfo("Deadlift", "Conventional", "Back"),
            ExerciseInfo("Pull-ups", null, "Back"),
            ExerciseInfo("Barbell Row", null, "Back"),
            ExerciseInfo("Lat Pulldown", null, "Back"),
            ExerciseInfo("Barbell Curl", null, "Biceps"),
        )

        private val LegExercises = listOf(
            ExerciseInfo("Squat", "Barbell", "Legs"),
            ExerciseInfo("Romanian Deadlift", null, "Legs"),
            ExerciseInfo("Leg Press", null, "Legs"),
            ExerciseInfo("Leg Curl", null, "Legs"),
            ExerciseInfo("Calf Raise", null, "Legs"),
        )

        private val ChestExercises = listOf(
            ExerciseInfo("Bench Press", "Barbell", "Chest"),
            ExerciseInfo("Incline Dumbbell Press", null, "Chest"),
            ExerciseInfo("Cable Fly", null, "Chest"),
            ExerciseInfo("Dips", null, "Triceps"),
            ExerciseInfo("Push-ups", null, "Chest"),
        )

        private val BackExercises = listOf(
            ExerciseInfo("Deadlift", "Conventional", "Back"),
            ExerciseInfo("Pull-ups", null, "Back"),
            ExerciseInfo("Barbell Row", null, "Back"),
            ExerciseInfo("Lat Pulldown", null, "Back"),
            ExerciseInfo("Face Pull", null, "Shoulders"),
        )

        private val ShoulderExercises = listOf(
            ExerciseInfo("Overhead Press", "Barbell", "Shoulders"),
            ExerciseInfo("Lateral Raise", null, "Shoulders"),
            ExerciseInfo("Rear Delt Fly", null, "Shoulders"),
            ExerciseInfo("Face Pull", null, "Shoulders"),
        )

        private val FullBodyExercises = listOf(
            ExerciseInfo("Squat", "Barbell", "Legs"),
            ExerciseInfo("Bench Press", "Barbell", "Chest"),
            ExerciseInfo("Deadlift", "Conventional", "Back"),
            ExerciseInfo("Overhead Press", "Barbell", "Shoulders"),
            ExerciseInfo("Pull-ups", null, "Back"),
        )
    }
}
