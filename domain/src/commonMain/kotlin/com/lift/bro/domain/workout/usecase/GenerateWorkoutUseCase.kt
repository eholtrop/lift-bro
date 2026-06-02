package com.lift.bro.domain.workout.usecase

import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.WorkoutTemplate
import com.lift.bro.domain.repositories.AIRepository
import com.lift.bro.domain.repositories.WorkoutGenerator
import com.lift.bro.domain.workout.WorkoutGenerationError

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
