package com.lift.bro.presentation.dashboard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.data.VariationRepository
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.toLocalDate
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


@Serializable
data class DashboardState(
    val items: List<DashboardListItem> = emptyList()
)

@Serializable
sealed class DashboardListItem {
    @Serializable
    data class LiftCard(val state: LiftCardState) : DashboardListItem()

    @Serializable
    data object Ad : DashboardListItem()

    @Serializable
    data object ReleaseNotes : DashboardListItem()
}

@Composable
fun rememberDashboardViewModel(): DashboardViewModel {
    return rememberSaveable(
        saver = object : Saver<DashboardViewModel, String> {
            override fun SaverScope.save(value: DashboardViewModel): String? {
                return Json.encodeToString(value.state.value)
            }

            override fun restore(value: String): DashboardViewModel? {
                return DashboardViewModel(Json.decodeFromString(value))
            }
        },
        init = {
            DashboardViewModel()
        }
    )
}

sealed interface DashboardEvent {
}

class DashboardViewModel(
    initialState: DashboardState = DashboardState(),
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.database.variantDataSource,
    setRepository: SetDataSource = dependencies.database.setDataSource,
    scope: CoroutineScope = GlobalScope
) {

    val input: Channel<DashboardEvent> = Channel()

    fun handleEvent(event: DashboardEvent) {
        input.trySend(event)
    }

    val state: StateFlow<DashboardState> = combine(
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
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = initialState,
    )
}