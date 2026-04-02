package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class GetVariationWithMostRepsUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {

    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Pair<Variation, Long>> =
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ).map { sets ->
            sets.groupBy { set -> set.variationId }
                .map { entry -> entry.key to entry.value.sumOf { it.reps } }
                .maxBy { it.second }
        }.flatMapLatest { (variationId, totalReps) ->
            variationRepository.listen(variationId)
                .filterNotNull()
                .map { variation -> variation to totalReps }
        }
}
