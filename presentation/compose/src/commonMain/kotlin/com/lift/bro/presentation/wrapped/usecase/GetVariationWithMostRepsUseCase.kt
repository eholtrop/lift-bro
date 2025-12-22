package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class GetVariationWithMostRepsUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ) = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll()
    ) { sets, variations ->
        sets.groupBy { set -> variations.first { it.id == set.variationId } }
            .map { entry -> entry.key to entry.value.sumOf { it.reps } }
            .maxBy { it.second }
    }
}
