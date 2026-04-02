package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate

/*
 * Gets the variation with the most weight moved between the given dates
 */
class GetVariationWithMostWeightMovedUseCase(
    private val setRepository: ISetRepository = dependencies.setRepository,
    private val variationRepository: IVariationRepository = dependencies.variationRepository,
) {

    /*
     * Fetches the total weight moved for between the given dates
     */
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Pair<Variation, Double>> = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll(),
    ) { sets, variations ->
        sets.groupBy { set -> variations.first { it.id == set.variationId } }
            .map { entry -> entry.key to entry.value.sumOf { it.weight } }
            .maxBy { it.second }
    }
}
