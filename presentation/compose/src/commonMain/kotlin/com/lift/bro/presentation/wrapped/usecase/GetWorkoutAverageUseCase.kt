package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.repositories.ISetRepository
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetWorkoutAverageUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
) {
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ) = setRepository.listenAll(
        startDate = startDate,
        endDate = endDate
    ).map {
        it.sumOf { it.reps } / it.groupBy { it.date.toLocalDate().dayOfYear }.size
    }
}
