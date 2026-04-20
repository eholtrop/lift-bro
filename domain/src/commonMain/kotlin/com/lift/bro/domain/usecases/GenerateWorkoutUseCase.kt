package com.lift.bro.domain.usecases

import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.WorkoutTemplate
import com.lift.bro.domain.repositories.AIRepository
import com.lift.bro.domain.repositories.WorkoutGenerator

sealed class WorkoutGenerationError : Exception() {
    data object ModelNotReady : WorkoutGenerationError()
    data object GenerationFailed : WorkoutGenerationError()
    data object ParseFailed : WorkoutGenerationError()
}

class GenerateWorkoutUseCase(
    private val aiRepository: AIRepository,
    private val generator: WorkoutGenerator,
) {
    suspend operator fun invoke(
        history: WorkoutHistory,
        preferences: WorkoutPreferences,
    ): Result<WorkoutTemplate> {
        if (!aiRepository.isModelReady()) {
            return Result.failure(WorkoutGenerationError.ModelNotReady)
        }

        return generator.generateWorkout(history, preferences)
    }
}

class GenerateSetRecommendationUseCase(
    private val generator: WorkoutGenerator,
) {
    suspend operator fun invoke(
        variationId: String,
        targetReps: Long,
        history: ExerciseHistory,
    ): Result<SetRecommendation> {
        return generator.suggestWeight(variationId, targetReps, history)
    }
}

class EnsureModelReadyUseCase(
    private val aiRepository: AIRepository,
) {
    fun getModelStatus() = aiRepository.getModelStatus()

    fun downloadModel() = aiRepository.downloadModel()

    suspend fun isReady() = aiRepository.isModelReady()
}
