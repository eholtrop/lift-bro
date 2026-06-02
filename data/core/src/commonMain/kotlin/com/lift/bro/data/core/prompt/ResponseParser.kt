package com.lift.bro.data.core.prompt

import com.lift.bro.domain.models.ExerciseTemplate
import com.lift.bro.domain.models.MuscleGroup
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.models.WorkoutTemplate
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

object ResponseParser {

    fun parseWorkoutResponse(response: String): WorkoutTemplate {
        val json = parseJson(response)

        val dayName = (json["dayName"] as? JsonPrimitive)?.content ?: "Workout Day"
        val focusString = (json["focus"] as? JsonPrimitive)?.content ?: "FullBody"
        val focus = try {
            MuscleGroup.valueOf(focusString)
        } catch (_: IllegalArgumentException) {
            MuscleGroup.FullBody
        }

        val exercises = (json["exercises"] as? JsonArray)
            ?.mapNotNull { element -> parseExercise(element as? JsonObject) }
            ?: emptyList()

        return WorkoutTemplate(
            dayName = dayName,
            focus = focus,
            exercises = exercises,
        )
    }

    fun parseWeightRecommendation(
        response: String,
        variationId: String
    ): SetRecommendation {
        val json = parseJson(response)

        val suggestedWeight = (json["suggestedWeight"] as? JsonPrimitive)?.content?.toDoubleOrNull() ?: 0.0
        val suggestedReps = (json["suggestedReps"] as? JsonPrimitive)?.content?.toLongOrNull() ?: 8L
        val confidence = (json["confidence"] as? JsonPrimitive)?.content?.toFloatOrNull() ?: 0.5f
        val rationale = (json["rationale"] as? JsonPrimitive)?.content ?: "Based on history"

        return SetRecommendation(
            variationId = variationId,
            suggestedWeight = suggestedWeight,
            suggestedReps = suggestedReps,
            confidence = confidence,
            basedOn = rationale,
        )
    }

    private fun parseExercise(json: JsonObject?): ExerciseTemplate? {
        if (json == null) return null
        return try {
            val name = (json["name"] as? JsonPrimitive)?.content ?: return null
            val variationName = (json["variationName"] as? JsonPrimitive)?.content
            val liftName = (json["liftName"] as? JsonPrimitive)?.content ?: name
            val sets = (json["sets"] as? JsonPrimitive)?.content?.toIntOrNull() ?: 4
            val reps = (json["reps"] as? JsonPrimitive)?.content ?: "8-12"
            val suggestedWeight = (json["suggestedWeight"] as? JsonPrimitive)?.content?.toDoubleOrNull()

            ExerciseTemplate(
                name = name,
                variationName = variationName,
                liftName = liftName,
                sets = sets,
                reps = reps,
                suggestedWeight = suggestedWeight,
            )
        } catch (_: NullPointerException) {
            null
        } catch (_: NumberFormatException) {
            null
        } catch (_: IllegalStateException) {
            null
        }
    }

    private fun parseJson(response: String): JsonObject {
        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
        }

        val cleanResponse = response
            .trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        return json.decodeFromString<JsonObject>(cleanResponse)
    }
}