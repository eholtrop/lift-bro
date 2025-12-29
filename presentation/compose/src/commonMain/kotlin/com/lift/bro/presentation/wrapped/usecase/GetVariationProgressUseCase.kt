package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate


data class VariationProgress(
    val minSet: LBSet,
    val maxSet: LBSet,
) {
    fun progress(bodyWeight: Boolean): Double = when (bodyWeight) {
        true -> ((maxSet.reps - minSet.reps) / minSet.reps).toDouble()
        else -> ((maxSet.weight - minSet.weight) / minSet.weight).toDouble()
    }
}

class GetVariationProgressUseCase(
    val setRepository: ISetRepository = dependencies.setRepository,
    val variationRepository: IVariationRepository = dependencies.variationRepository,
) {
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Map<Variation, VariationProgress>> = combine(
        setRepository.listenAll(
            startDate = startDate,
            endDate = endDate
        ),
        variationRepository.listenAll()
    ) { sets, variations ->
        sets.groupBy { set -> variations.first { it.id == set.variationId } }
            .filter { it.value.isNotEmpty() }
            .mapValues { (variation, variationSets) ->
                val orderedSets = variationSets
                    .groupBy { it.date.toLocalDate() }
                    .toList()
                    .sortedByDescending { it.first }

                val minSet = when (variation.bodyWeight) {
                    true -> orderedSets.last().second.maxBy { it.reps }
                    // find last one rep max, if none then get the last sets max weight lifted
                    else -> orderedSets.lastOrNull { it.second.any { it.reps == 1L } }?.second?.lastOrNull { it.reps == 1L }
                        ?: orderedSets.last().second.maxBy { it.weight }
                }
                val maxSet = when (variation.bodyWeight) {
                    true -> orderedSets.first().second.maxBy { it.reps }
                    // find last one rep max, if none then get the last sets max weight lifted
                    else -> orderedSets.firstOrNull { it.second.any { it.reps == 1L } }?.second?.firstOrNull { it.reps == 1L }
                        ?: orderedSets.first().second.maxBy { it.weight }
                }

                VariationProgress(
                    minSet = minSet,
                    maxSet = maxSet,
                )
            }
    }
}
