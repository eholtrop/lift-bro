package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate

class GetMostConsistentDayUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Pair<DayOfWeek, Int>> = setRepository.listenAll(
        startDate = startDate,
        endDate = endDate
    ).map {
        it.groupBy { set -> set.date.toLocalDate().dayOfWeek }.map { it.key to it.value.map { it.date.toLocalDate() }.toSet().size }.maxBy { it.second }
    }
}
