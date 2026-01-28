package com.lift.bro.presentation.wrapped.summary

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.lift.bro.domain.models.Goal
import com.lift.bro.mvi.Interactor
import com.lift.bro.mvi.compose.rememberInteractor
import com.lift.bro.presentation.wrapped.HeavyThing
import com.lift.bro.presentation.wrapped.LocalWrappedYear
import com.lift.bro.presentation.wrapped.heavyThings
import com.lift.bro.presentation.wrapped.usecase.GetGoalsUseCase
import com.lift.bro.presentation.wrapped.usecase.GetTotalRepsUseCase
import com.lift.bro.presentation.wrapped.usecase.GetTotalWeightMovedUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationConsistencyUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationProgressUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationWithMostRepsUseCase
import com.lift.bro.presentation.wrapped.usecase.GetVariationWithMostWeightMovedUseCase
import com.lift.bro.ext.flow.combine
import com.lift.bro.utils.fullName
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

@Composable
fun rememberWrappedSummaryInteractor(
    year: Int = LocalWrappedYear.current,
    // Weight use cases
    getTotalWeightMovedUseCase: GetTotalWeightMovedUseCase = GetTotalWeightMovedUseCase(),
    getVariationWithMostWeightMovedUseCase: GetVariationWithMostWeightMovedUseCase =
        GetVariationWithMostWeightMovedUseCase(),

    // Reps use cases
    getTotalRepsUseCase: GetTotalRepsUseCase = GetTotalRepsUseCase(),
    getVariationWithMostRepsUseCase: GetVariationWithMostRepsUseCase = GetVariationWithMostRepsUseCase(),

    // Consistency use cases
    getVariationConsistencyUseCase: GetVariationConsistencyUseCase = GetVariationConsistencyUseCase(),

    // Progress use cases
    getVariationProgressUseCase: GetVariationProgressUseCase = GetVariationProgressUseCase(),

    // Goal use cases
    getGoalsUseCase: GetGoalsUseCase = GetGoalsUseCase(),
): Interactor<WrappedSummaryState?, Nothing> = rememberInteractor(
    initialState = null,
    source = {
        combine(
            getTotalWeightMovedUseCase(),
            getVariationWithMostWeightMovedUseCase(),
            getTotalRepsUseCase(),
            getVariationWithMostRepsUseCase(),
            getVariationConsistencyUseCase(),
            getVariationProgressUseCase()
                .map { it.filterValues { it != null }.mapValues { it.value!! } },
            getGoalsUseCase(),
        ) { twm, variationTwm, totalReps, variationReps, consistency, progress, goals ->
            WrappedSummaryState(
                weight = heavyThings.random().let { heavyThing ->
                    WrappedSummaryWeightState(
                        totalWeightMoved = twm,
                        heaviestVariationWeight = variationTwm.second,
                        heaviestVariationName = variationTwm.first.fullName,
                        numOfHeavyThings = twm / heavyThing.weight,
                        heavyThing = heavyThing
                    )
                },
                reps = WrappedSummaryRepsState(
                    totalReps = totalReps.toInt(),
                    variationReps = variationReps.second.toInt(),
                    variationName = variationReps.first.fullName,
                    repsPerDay = (totalReps / (if (year % 4 == 0) 366 else 365)).toInt()

                ),
                consistencies = listOf(
                    // most consistent month
                    consistency.keys.groupBy { it.month }.maxBy { it.value.size }.let { (month, sets) ->
                        WrappedSummaryConsistencyState(
                            title = month.name,
                            occurrences = sets.size
                        )
                    },
                    // most consistent day
                    consistency.keys.groupBy { it.dayOfWeek }
                        .maxBy { it.value.size }
                        .let { (dayOfWeek, sets) ->
                            WrappedSummaryConsistencyState(
                                title = dayOfWeek.name,
                                occurrences = sets.size
                            )
                        },
                    // most consistent workout
                    consistency.values.flatten()
                        .groupBy { it }
                        .maxBy { it.value.size }
                        .let { (variation, sets) ->
                            WrappedSummaryConsistencyState(
                                title = variation.fullName,
                                occurrences = sets.size
                            )
                        }
                ),
                progression = progress.toList()
                    .filter { it.second.progress(it.first.bodyWeight ?: false).isNaN().not() }
                    .sortedByDescending { it.second.progress(it.first.bodyWeight ?: false) }
                    .take(3)
                    .map { (variation, progress) ->
                        WrappedSummaryProgressState(
                            title = variation.fullName,
                            progress = when (variation.bodyWeight) {
                                true -> (progress.maxSet.reps - progress.minSet.reps) / progress.minSet.reps
                                else -> (progress.maxSet.weight - progress.minSet.weight) / progress.minSet.weight
                            }.toDouble(),
                            minWeight = progress.minSet.weight,
                            maxWeight = progress.maxSet.weight,
                        )
                    },
                goals = goals,
            )
        }
    }
)

@Serializable
@Immutable
data class WrappedSummaryState(
    val weight: WrappedSummaryWeightState,
    val reps: WrappedSummaryRepsState,
    val consistencies: List<WrappedSummaryConsistencyState>,
    val progression: List<WrappedSummaryProgressState>,
    val goals: List<Goal>,
)

@Serializable
data class WrappedSummaryWeightState(
    val totalWeightMoved: Double,
    val heaviestVariationWeight: Double,
    val heaviestVariationName: String,
    val numOfHeavyThings: Double,
    val heavyThing: HeavyThing,
)

@Serializable
data class WrappedSummaryRepsState(
    val totalReps: Int,
    val variationReps: Int,
    val variationName: String,
    val repsPerDay: Int,
)

@Serializable
data class WrappedSummaryConsistencyState(
    val title: String,
    val occurrences: Int,
)

@Serializable
data class WrappedSummaryProgressState(
    val title: String,
    val progress: Double,
    val minWeight: Double,
    val maxWeight: Double,
)
