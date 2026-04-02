package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.repositories.ISetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetTotalWeightMovedUseCase(
    private val setRepository: ISetRepository = dependencies.setRepository,
) {

    /*
     * Fetches the total weight moved for between the given dates
     */
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Double> = setRepository.listenAll(
        startDate = startDate,
        endDate = endDate
    ).map { sets -> sets.sumOf { it.weight * it.reps } }
}
