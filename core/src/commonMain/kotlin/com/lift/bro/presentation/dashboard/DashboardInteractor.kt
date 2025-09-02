package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.utils.toLocalDate
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
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
): Interactor<DashboardState, DashboardEvent> = rememberInteractor(
    initialState = DashboardState(),
    sideEffects = listOf { state, event ->
        when (event) {
            DashboardEvent.AddLiftClicked -> navCoordinator.present(Destination.EditLift(null))
            is DashboardEvent.LiftClicked -> navCoordinator.present(Destination.EditLift(event.liftId))
        }
    },
    source = combine(
        liftRepository.listenAll(),
        variationRepository.listenAll(),
        setRepository.listenAll(),
        flow {
            emit(SubscriptionType.Pro)
            if (!Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.containsKey("pro")) {
                emit(SubscriptionType.None)
            }
        }
    ) { lifts, variations, sets, subType ->
        val liftItems: List<DashboardListItem> =
            variations.groupBy { it.lift?.id }.map { (liftId, liftVariations) ->
                val liftSets =
                    sets.filter { set -> liftVariations.any { set.variationId == it.id } }
                lifts.firstOrNull { it.id == liftId }?.let {
                    DashboardListItem.LiftCard(
                        LiftCardState(
                            lift = it,
                            values = liftSets.groupBy { it.date.toLocalDate() }.map {
                                it.key to LiftCardData(
                                    it.value.maxOf { it.weight },
                                    it.value.maxOf { it.reps }.toInt(),
                                    it.value.maxOfOrNull { it.rpe ?: 0 },
                                )
                            }.sortedByDescending { it.first }.take(5).reversed(),
                        )
                    )
                }
            }.filterNotNull()
                .sortedBy { it.state.lift.name }
                .sortedByDescending { item -> variations.any { item.state.lift.id == it.lift?.id && it.favourite } }


        DashboardState(
            items = liftItems.toMutableList().apply {
                if (this.size > 2) {
                    add(2, DashboardListItem.Ad)
                } else {
                    add(DashboardListItem.Ad)
                }
                add(0, DashboardListItem.ReleaseNotes)
            }
        )
    }
)