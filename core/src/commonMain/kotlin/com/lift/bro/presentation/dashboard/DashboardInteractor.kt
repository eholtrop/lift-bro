package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.utils.debug
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable


@Serializable
data class DashboardState(
    val items: List<DashboardListItem> = emptyList(),
)

@Serializable
sealed class DashboardListItem {
    @Serializable
    data class LiftCard(val state: LiftCardState): DashboardListItem()

    @Serializable
    data object Ad: DashboardListItem()

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
    variationRepository: IVariationRepository = dependencies.database.variantDataSource,
    setRepository: SetDataSource = dependencies.database.setDataSource,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<DashboardState, DashboardEvent> = rememberInteractor<DashboardState, DashboardEvent>(
    initialState = DashboardState(),
    sideEffects = listOf { state, event ->
        when (event) {
            DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
            is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.LiftDetails(event.liftId))
        }
    },
    source = variationRepository.listenAll()
        .flatMapLatest { variations ->
            val cards = variations.groupBy { it.lift }.map { (lift, liftVariations) ->
                dependencies.database.setDataSource.listenAllForLift(lift?.id ?: "", 20)
                    .map { sets ->
                        lift?.let {
                            DashboardListItem.LiftCard(
                                LiftCardState(
                                    lift = it,
                                    values = sets.groupBy { it.date.toLocalDate() }.map {
                                        it.key to LiftCardData(
                                            it.value.maxOf { it.weight },
                                            it.value.maxOf { it.reps }.toInt(),
                                            it.value.maxOfOrNull { it.rpe ?: 0 },
                                        )
                                    }.sortedByDescending { it.first }.take(5).reversed(),
                                    maxWeight = liftVariations.maxOfOrNull { it.oneRepMax?.weight ?: 0.0 },
                                    maxReps = liftVariations.maxOfOrNull { it.maxReps?.reps?.toDouble() ?: 0.0 },
                                )
                            )
                        }
                    }.filterNotNull().map { it as DashboardListItem }
            }
            combine(*cards.toTypedArray()) { arr ->
                val variations = dependencies.database.variantDataSource.getAll()
                arr.toList()
                    .sortedBy { (it as? DashboardListItem.LiftCard)?.state?.lift?.name }
                    .sortedByDescending { item -> variations.any { (item as? DashboardListItem.LiftCard)?.state?.lift?.id == it.lift?.id && it.favourite } }
            }
                .map { items ->
                    DashboardState(
                        items = items.toMutableList().apply {
                            if (this.size > 2) {
                                add(2, DashboardListItem.Ad)
                            } else {
                                add(DashboardListItem.Ad)
                            }
                            add(0, DashboardListItem.ReleaseNotes)
                            add(DashboardListItem.AddLiftButton)
                        }
                    )
                }
        },
)