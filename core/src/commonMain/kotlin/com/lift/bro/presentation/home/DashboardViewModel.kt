package com.lift.bro.presentation.home

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.lift.bro.BackupService
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.defaultSbdLifts
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.SubscriptionType
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import com.lift.bro.utils.toLocalDate
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DashboardViewModel(
    initialState: DashboardState? = null,
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.database.variantDataSource,
    setRepository: SetDataSource = dependencies.database.setDataSource,
    scope: CoroutineScope = GlobalScope
) {
    val state = combine(
        liftRepository.listenAll(),
        variationRepository.listenAll(),
        setRepository.listenAll(),
        dependencies.database.logDataSource.getAll().asFlow().mapToList(Dispatchers.IO),
        flow {
            emit(SubscriptionType.Pro)
            if (!Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.containsKey("pro")) {
                emit(SubscriptionType.None)
            }
        },
    ) { lifts, variations, sets, logs, subType ->
        DashboardState(
            showEmpty = lifts.isEmpty(),
            items = lifts.map { lift ->
                val liftVariations = variations.filter { it.lift?.id == lift.id }
                val liftSets =
                    sets.filter { set -> liftVariations.any { set.variationId == it.id } }
                DashboardListItem.LiftCard(
                    LiftCardState(
                        lift = lift,
                        values = liftSets.groupBy { it.date.toLocalDate() }
                            .map {
                                it.key to LiftCardData(
                                    it.value.maxOf { it.weight },
                                    it.value.maxOf { it.reps }.toInt(),
                                    it.value.maxOfOrNull { it.rpe ?: 0 },
                                )
                            }.sortedByDescending { it.first }.take(5).reversed(),
                    )
                )
            }.sortedBy { it.state.lift.name.toLowerCase(Locale.current) }
                .toMutableList<DashboardListItem>().apply {
                    if (this.isEmpty()) {

                    } else if (this.size < 2) {
                        this.add(DashboardListItem.Ad)
                    } else {
                        if (subType == SubscriptionType.None) {
                            this.add(2, DashboardListItem.Ad)
                        }
                    }
                }.toList(),
            workouts = sets.groupBy { it.date.toLocalDate() }.map { dateSetsEntry ->
                val date = dateSetsEntry.key
                Workout(
                    date = date,
                    exercises = dateSetsEntry.value.groupBy { it.variationId }.map { map ->
                        val variation = variations.firstOrNull { it.id == map.key }
                        variation?.let {
                            Exercise(
                                variation = variation,
                                sets = map.value
                            )
                        }
                    }.filterNotNull()
                )
            },
            logs = logs.map {
                LiftingLog(
                    id = it.id,
                    date = it.date,
                    notes = it.notes ?: "",
                    vibe = it.vibe_check?.toInt(),
                )
            },
        )
    }
        .stateIn(scope, SharingStarted.Eagerly, initialState)

    fun handleEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.RestoreDefaultLifts -> GlobalScope.launch {
                BackupService.restore(defaultSbdLifts)
            }
        }
    }
}