package com.lift.bro

import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ILiftingLogRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository

class ClearUserDataUseCase(
    private val liftRepository: ILiftRepository,
    private val variationRepository: IVariationRepository,
    private val setRepository: ISetRepository,
    private val liftingLogRepository: ILiftingLogRepository,
    private val workoutRepository: IWorkoutRepository,
    private val exerciseRepository: IExerciseRepository,
) {
    suspend operator fun invoke() {
        setRepository.deleteAll()
        variationRepository.deleteAll()
        liftRepository.deleteAll()
        liftingLogRepository.deleteAll()
        workoutRepository.deleteAll()
        exerciseRepository.deleteAll()
    }
}
