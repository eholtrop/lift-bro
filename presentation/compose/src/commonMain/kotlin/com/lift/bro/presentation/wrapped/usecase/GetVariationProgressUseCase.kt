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
                val dateSets: List<Pair<LocalDate, List<LBSet>>> = variationSets
                    .groupBy { it.date.toLocalDate() }
                    .toList()
                    .sortedByDescending { it.first }

                val minSet: LBSet = when (variation.bodyWeight) {
                    true -> dateSets.last().second.maxBy { it.reps }
                // find last one rep max
                    else -> dateSets.lastOrNull { dateSet -> dateSet.second.any { set -> set.reps == 1L } }
                        ?.second?.filter { it.reps == 1L }?.maxBy { it.weight }
                    // if none then get the last sets max weight lifted
                        ?: dateSets.last().second.maxBy { it.weight }
                }
                val maxSet: LBSet = when (variation.bodyWeight) {
                    true -> dateSets.first().second.maxBy { it.reps }
                    // find first one rep max
                    else -> dateSets.firstOrNull { dateSet -> dateSet.second.any { set -> set.reps == 1L } }
                        ?.second?.filter { it.reps == 1L }?.maxBy { it.weight }
                    //if none then get the first sets max weight lifted
                        ?: dateSets.first().second.maxBy { it.weight }
                }

                VariationProgress(
                    minSet = minSet,
                    maxSet = maxSet,
                )
            }
    }
}
