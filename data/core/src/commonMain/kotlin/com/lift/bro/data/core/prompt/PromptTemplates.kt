package com.lift.bro.data.core.prompt

import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences

object PromptTemplates {

    fun generateWorkoutPrompt(
        history: WorkoutHistory,
        preferences: WorkoutPreferences
    ): String {
        val historyText = buildHistoryText(history)
        val preferencesText = buildPreferencesText(preferences)

        return """
You are an expert personal fitness trainer. Generate a workout based on the user's history and preferences.

USER'S WORKOUT HISTORY (last 30 days):
$historyText

USER'S PREFERENCES:
$preferencesText

Generate a balanced workout that:
1. Builds on their strengths and addresses weaknesses
2. Provides progressive overload
3. Matches their experience level and goals

Respond in JSON format with the following structure:
{
  "dayName": "Day name (e.g., Push Day)",
  "focus": "MUSCLE_GROUP (one of: Chest, Back, Shoulders, Biceps, Triceps, Legs, Core, FullBody, Push, Pull, LegsLower, LegsUpper)",
  "exercises": [
    {
      "name": "Exercise name",
      "variationName": "Variation (e.g., Barbell, Dumbbell)",
      "liftName": "Primary lift name",
      "sets": 4,
      "reps": "8-12",
      "suggestedWeight": 135.0
    }
  ]
}

Only respond with valid JSON, no additional text.
        """.trimIndent()
    }

    @Suppress("UNUSED_PARAMETER")
    fun weightSuggestionPrompt(
        variationId: String,
        targetReps: Long,
        history: ExerciseHistory
    ): String {
        val historyText = if (history.sets.isNotEmpty()) {
            val best = history.sets.maxByOrNull { it.estimatedMax }
            """
Recent performance:
- Best: ${best?.weight}lbs x ${best?.reps} reps (estimated max: ${best?.estimatedMax?.toInt()}lbs)
- Average weight: ${history.sets.map { it.weight }.average().toInt()}lbs
- Average reps: ${history.sets.map { it.reps }.average().toInt()}
            """.trimIndent()
        } else {
            "No previous history for this exercise."
        }

        return """
You are an expert fitness trainer. Based on the user's history, suggest appropriate weight for target reps.

EXERCISE HISTORY:
$historyText

Target: $targetReps reps

Respond in JSON format:
{
  "suggestedWeight": 135.0,
  "suggestedReps": 8,
  "confidence": 0.8,
  "rationale": "Based on your estimated max of X, aiming for Y reps"
}

Only respond with valid JSON, no additional text.
        """.trimIndent()
    }

    private fun buildHistoryText(history: WorkoutHistory): String {
        if (history.exerciseHistory.isEmpty()) {
            return "No workout history available. User is a beginner."
        }

        return buildString {
            appendLine("Summary:")
            appendLine("- Total workouts: ${history.summary.totalWorkouts}")
            appendLine("- Average workouts/week: ${history.summary.averageWorkoutsPerWeek}")
            appendLine("- Current streak: ${history.summary.currentStreak} days")

            if (history.summary.personalRecords.isNotEmpty()) {
                appendLine("\nPersonal Records:")
                history.summary.personalRecords.take(5).forEach { pr ->
                    val prLine = "- ${pr.liftName} (${pr.variationName}): " +
                        "${pr.weight.toInt()}lbs x ${pr.reps} reps " +
                        "(${pr.estimatedMax.toInt()}lbs e1rm)"
                    appendLine(prLine)
                }
            }

            appendLine("\nExercise Performance:")
            history.exerciseHistory.take(10).forEach { entry ->
                val entryLine = "- ${entry.liftName} (${entry.variationName}): " +
                    "best ${entry.bestWeight.toInt()}lbs x ${entry.bestReps}, " +
                    "avg ${entry.averageWeight.toInt()}lbs x ${entry.averageReps.toInt()}, " +
                    "${entry.frequencyPerWeek.toInt()}/week"
                appendLine(entryLine)
            }
        }
    }

    private fun buildPreferencesText(preferences: WorkoutPreferences): String {
        return buildString {
            appendLine("- Goal: ${preferences.goal.name}")
            appendLine("- Experience: ${preferences.experienceLevel.name}")
            appendLine("- Days per week: ${preferences.daysPerWeek}")
            if (preferences.focusMuscleGroups.isNotEmpty()) {
                appendLine("- Focus: ${preferences.focusMuscleGroups.joinToString { it.name }}")
            }
        }
    }
}
