package com.lift.bro.presentation.wrapped.usecase

import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import com.lift.bro.utils.debug
import com.lift.bro.utils.fullName
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate
import kotlin.reflect.KClass


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
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
    ): Flow<Map<Variation, VariationProgress?>> = variationRepository.listenAll()
        .flatMapLatest { variations ->
            combine(
                flows = variations.map { variation ->
                    combine(
                        setRepository.getEarliestOneRepMax(
                            startDate = startDate,
                            endDate = endDate,
                            variation = variation,
                        ),
                        setRepository.getLatestOneRepMax(
                            startDate = startDate,
                            endDate = endDate,
                            variation = variation,
                        ),
                        setRepository.getEarliestMaxSet(
                            startDate = startDate,
                            endDate = endDate,
                            variation = variation,
                        ),
                        setRepository.getLatestMaxSet(
                            startDate = startDate,
                            endDate = endDate,
                            variation = variation,
                        ),
                    ) { firstOrm, lastOrm, firstSet, lastSet /* Best Set */ ->
                        variation to if (firstSet != null && lastSet != null) {
                            if (variation.bodyWeight == true) {
                                VariationProgress(
                                    minSet = firstSet,
                                    maxSet = lastSet,
                                )
                            } else {
                                VariationProgress(
                                    minSet = firstOrm ?: firstSet,
                                    maxSet = lastOrm ?: lastSet,
                                )
                            }
                        } else {
                            null
                        }
                    }
                }.toTypedArray()
            ) { arr ->
                arr.toMap()
            }
        }
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun ISetRepository.getEarliestOneRepMax(
    startDate: LocalDate?,
    endDate: LocalDate?,
    variation: Variation,
): Flow<LBSet?> = this.listenAll(
    startDate = startDate,
    endDate = endDate,
    variationId = variation.id,
    reps = 1L,
    limit = 1L,
    order = Order.Ascending,
    sorting = Sorting.date,
).map { it.firstOrNull() }
    .getMaxInDay(this, variation.bodyWeight ?: false)

private fun ISetRepository.getLatestOneRepMax(
    startDate: LocalDate?,
    endDate: LocalDate?,
    variation: Variation,
): Flow<LBSet?> = this.listenAll(
    startDate = startDate,
    endDate = endDate,
    variationId = variation.id,
    reps = 1L,
    limit = 1L,
    order = Order.Descending,
    sorting = Sorting.date,
).map { it.firstOrNull() }
    .getMaxInDay(this, variation.bodyWeight ?: false)

private fun ISetRepository.getEarliestMaxSet(
    startDate: LocalDate?,
    endDate: LocalDate?,
    variation: Variation,
): Flow<LBSet?> = this.listenAll(
    startDate = startDate,
    endDate = endDate,
    variationId = variation.id,
    limit = 1L,
    order = Order.Ascending,
    sorting = Sorting.date,
).map { it.firstOrNull() }
    .getMaxInDay(this, variation.bodyWeight ?: false)

private fun ISetRepository.getLatestMaxSet(
    startDate: LocalDate?,
    endDate: LocalDate?,
    variation: Variation,
): Flow<LBSet?> = this.listenAll(
    startDate = startDate,
    endDate = endDate,
    variationId = variation.id,
    limit = 1L,
    order = Order.Descending,
    sorting = Sorting.date,
).map { it.firstOrNull() }
    .getMaxInDay(this, variation.bodyWeight ?: false)


@OptIn(ExperimentalCoroutinesApi::class)
private fun Flow<LBSet?>.getMaxInDay(
    setRepository: ISetRepository,
    bodyWeight: Boolean,
) = this.flatMapLatest {
    it?.let { set ->
        setRepository.listenAll(
            startDate = set.date.toLocalDate(),
            endDate = set.date.toLocalDate(),
            variationId = set.variationId,
        ).map {
            if (bodyWeight) {
                it.maxByOrNull { it.reps }
            } else {
                it.maxByOrNull { it.weight }
            }
        }
    } ?: flow { emit(null) }
}

