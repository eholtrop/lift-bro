package com.lift.bro.domain.workout.usecase

import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.repositories.WorkoutGenerator

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
