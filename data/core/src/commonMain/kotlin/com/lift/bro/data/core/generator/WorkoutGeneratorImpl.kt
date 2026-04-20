package com.lift.bro.data.core.generator

import com.lift.bro.data.core.prompt.PromptTemplates
import com.lift.bro.data.core.prompt.ResponseParser
import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.WorkoutTemplate
import com.lift.bro.domain.repositories.AIRepository
import com.lift.bro.domain.repositories.WorkoutGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WorkoutGeneratorImpl(
    private val aiRepository: AIRepository,
    private val ruleBasedGenerator: RuleBasedGenerator = RuleBasedGenerator(),
) : WorkoutGenerator {

    override suspend fun generateWorkout(
        history: WorkoutHistory,
        preferences: WorkoutPreferences
    ): Result<WorkoutTemplate> = withContext(Dispatchers.Default) {
        if (!aiRepository.isModelReady()) {
            return@withContext ruleBasedGenerator.generateWorkout(history, preferences)
        }

        val prompt = PromptTemplates.generateWorkoutPrompt(history, preferences)

        aiRepository.generate(prompt)
            .mapCatching { response ->
                ResponseParser.parseWorkoutResponse(response)
            }
            .recoverCatching { error ->
                ruleBasedGenerator.generateWorkout(history, preferences).getOrThrow()
            }
    }

    override suspend fun suggestWeight(
        variationId: String,
        targetReps: Long,
        history: ExerciseHistory
    ): Result<SetRecommendation> = withContext(Dispatchers.Default) {
        if (!aiRepository.isModelReady()) {
            return@withContext ruleBasedGenerator.suggestWeight(variationId, targetReps, history)
        }

        val prompt = PromptTemplates.weightSuggestionPrompt(variationId, targetReps, history)

        aiRepository.generate(prompt)
            .mapCatching { response ->
                ResponseParser.parseWeightRecommendation(response, variationId)
            }
            .recoverCatching { error ->
                ruleBasedGenerator.suggestWeight(variationId, targetReps, history).getOrThrow()
            }
    }
}
