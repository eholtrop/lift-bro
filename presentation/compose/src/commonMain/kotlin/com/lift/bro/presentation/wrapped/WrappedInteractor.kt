package com.lift.bro.presentation.wrapped

import androidx.compose.runtime.Composable
import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.today
import com.lift.bro.utils.fullName
import kotlinx.coroutines.flow.combine

val heavyThings = listOf(
    HeavyThing(
        name = "Blue Whale",
        weight = 300000.0,
        icon = "\uD83D\uDC0B"
    ),
    HeavyThing(
        name = "Elephant",
        weight = 15432.0,
        icon = "\uD83D\uDC18"
    ),
    HeavyThing(
        name = "Truck",
        weight = 5000.0,
        icon = "\uD83D\uDEFB",
    )
)

@Composable
fun rememberWrappedInteractor(
    setRepository: ISetRepository = dependencies.setRepository,
    variationRepository: IVariationRepository = dependencies.variationRepository,
): Interactor<WrappedState, WrappedEvents> {
    return rememberInteractor(
        initialState = WrappedState(),
        source = {
            combine(
                setRepository.listenAll(),
                variationRepository.listenAll(),
            ) { sets, variations ->
                val variationSets = sets.groupBy { set -> variations.first { it.id == set.variationId } }


                WrappedState(
                    pages = listOf(
                        WrappedPageState.Tenure,
                        WrappedPageState.Weight(
                            totalWeightMoved = sets.sumOf { it.weight * it.reps },
                            heavyThing = heavyThings.toList().random(),
                            heaviestVariation = variationSets.map { entry -> entry.key.fullName to entry.value.sumOf { it.weight } }
                                .maxBy { it.second }
                        ),
                        WrappedPageState.Reps(
                            totalReps = sets.sumOf { it.reps },
                            dailyAverage = sets.sumOf { it.reps } / if (today.year % 4 == 0) 366 else 365,
                            workoutAverage = sets.sumOf { it.reps } / sets.groupBy { it.date.toLocalDate().dayOfYear }.size ,
                            mostRepsLift = variationSets.map { entry -> entry.key.fullName to entry.value.sumOf { it.reps } }.maxBy { it.second }
                        ),
                        WrappedPageState.Consistency(
                            dates = sets.map { it.date.toLocalDate() }.toSet()
                        ),
                        WrappedPageState.Progress(
                            items = variationSets.toList()
                                .filter { it.second.isNotEmpty() }
                                .sortedBy { it.first.fullName }
                                .map { (variation, variationSets) ->
                                    val orderedSets = variationSets
                                        .groupBy { it.date.toLocalDate() }
                                        .toList()
                                        .sortedByDescending { it.first }

                                    val minSet = when (variation.bodyWeight) {
                                        // find last one rep max, if none then get the last sets max weight lifted
                                        true -> orderedSets.lastOrNull()?.second?.maxBy { it.reps }
                                        else -> orderedSets.lastOrNull { it.second.any { it.reps == 1L } }?.second?.firstOrNull { it.reps == 1L }
                                            ?: orderedSets.lastOrNull()?.second?.maxBy { it.weight }
                                    }
                                    val maxSet = when (variation.bodyWeight) {
                                        // find last one rep max, if none then get the last sets max weight lifted
                                        true -> orderedSets.firstOrNull()?.second?.maxBy { it.reps }
                                        else -> orderedSets.firstOrNull { it.second.any { it.reps == 1L } }?.second?.firstOrNull { it.reps == 1L }
                                            ?: orderedSets.firstOrNull()?.second?.maxBy { it.weight }
                                    }

                                    WrappedPageState.ProgressItemState(
                                        title = variation.fullName,
                                        minWeight = minSet?.let {
                                            WrappedPageState.ProgressItemWeight(
                                                date = minSet.date.toLocalDate(),
                                                weight = minSet.weight,
                                                reps = minSet.reps,
                                            )
                                        },
                                        maxWeight = maxSet?.let {
                                            WrappedPageState.ProgressItemWeight(
                                                date = maxSet.date.toLocalDate(),
                                                weight = maxSet.weight,
                                                reps = maxSet.reps,
                                            )
                                        },
                                        progress = when (variation.bodyWeight) {
                                            true -> ((maxSet?.reps ?: 0L) - (minSet?.reps ?: 0L)) / (minSet?.reps ?: 1L).toDouble()
                                            else -> ((maxSet?.weight ?: 0.0) - (minSet?.weight ?: 0.0)) / (minSet?.weight ?: 1.0)
                                        }.let {
                                            if (it.isNaN()) 0.0 else it
                                        }
                                    )
                                }.sortedByDescending { it.progress }
                        ),
                        WrappedPageState.Goals,
                        WrappedPageState.Summary(
                            sets = sets
                        ),
                    )
                )
            }
        }
    )
}
