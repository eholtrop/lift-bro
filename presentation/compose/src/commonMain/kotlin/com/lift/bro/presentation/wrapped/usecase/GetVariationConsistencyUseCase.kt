package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.`ktx-datetime`.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

class GetVariationConsistencyUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {

    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Map<LocalDate, List<Variation>>> = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll()
    ) { sets, variations ->
        sets.groupBy { set -> set.date.toLocalDate() }
            .mapValues { (_, sets) ->
                variations.filter { variation -> sets.any { it.variationId == variation.id } }
            }
    }
}
