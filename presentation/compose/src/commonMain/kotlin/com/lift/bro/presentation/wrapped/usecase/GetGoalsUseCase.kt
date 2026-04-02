package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.goalsRepository
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.repositories.IGoalRepository
import kotlinx.coroutines.flow.Flow

class GetGoalsUseCase(
    val goalsRepository: IGoalRepository = dependencies.goalsRepository,
) {
    operator fun invoke(): Flow<List<Goal>> = goalsRepository.getAll()
}
