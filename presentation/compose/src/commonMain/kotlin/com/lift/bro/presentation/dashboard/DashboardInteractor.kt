package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.utils.debug
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

typealias DashboardInteractor = Interactor<DashboardState, DashboardEvent>


@Serializable
sealed interface DashboardState
@Serializable
data class Loaded(val items: List<DashboardListItem>): DashboardState
@Serializable
data object Loading: DashboardState

@Serializable
sealed class DashboardListItem {

    @Serializable
    sealed class LiftCard: DashboardListItem() {

        @Serializable
        data class Loaded(val state: LiftCardState): LiftCard()

        @Serializable
        data object Loading: LiftCard()
    }

    @Serializable
    data object ReleaseNotes: DashboardListItem()

    @Serializable
    data object AddLiftButton: DashboardListItem()
}

sealed interface DashboardEvent {
    data object AddLiftClicked: DashboardEvent
    data class LiftClicked(val liftId: String): DashboardEvent
}

@Composable
fun rememberDashboardInteractor(
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.variationRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): DashboardInteractor = rememberInteractor<DashboardState, DashboardEvent>(
    initialState = Loading,
    sideEffects = listOf { state, event ->
        when (event) {
            DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
            is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.LiftDetails(event.liftId))
        }
    },
    source = {
        combine(
            liftRepository.listenAll().onStart { emit(emptyList()) },
            variationRepository.listenAll().onStart { emit(emptyList()) }
        ) { lifts, variations -> lifts to variations }
            .flatMapLatest { (lifts, variations) ->
                val variationsByLift = variations.groupBy { it.lift?.id }
                val cards: List<Flow<DashboardListItem?>> = lifts.map { lift ->
                    val liftVariations = variationsByLift[lift.id] ?: emptyList()
                    setRepository.listenAllForLift(lift.id, limit = 50)
                        .map { sets ->
                            DashboardListItem.LiftCard.Loaded(
                                LiftCardState(
                                    lift = lift,
                                    values = sets.groupBy { it.date.toLocalDate() }.map {
                                        it.key to LiftCardData(
                                            it.value.maxOf { it.weight },
                                            it.value.maxOf { it.reps }.toInt(),
                                            it.value.maxOfOrNull { it.rpe ?: 0 },
                                        )
                                    }.sortedByDescending { it.first }.take(5).reversed(),
                                    maxWeight = liftVariations.maxOfOrNull {
                                        it.oneRepMax?.weight ?: 0.0
                                    },
                                    maxReps = liftVariations.maxOfOrNull {
                                        it.maxReps?.reps?.toDouble() ?: 0.0
                                    },
                                )
                            )
                        }
                }

                combine(
                    *cards.map { it.onStart { emit(DashboardListItem.LiftCard.Loading) } }
                        .toTypedArray()
                ) { it.toList().filterNotNull() }
                    .debounce { 100L }
                    .map { cards ->
                        cards.sortedBy { (it as? DashboardListItem.LiftCard.Loaded)?.state?.lift?.name }
                            .sortedByDescending { item ->
                                variations.any {
                                    (item as? DashboardListItem.LiftCard.Loaded)?.state?.lift?.id == it.lift?.id && it.favourite
                                }
                            }
                    }
                    .map { items ->
                        Loaded(
                            items = items.toMutableList().apply {
                                add(0, DashboardListItem.ReleaseNotes)
                                add(DashboardListItem.AddLiftButton)
                            }
                        )
                    }
            }
    },
)
