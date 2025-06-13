package com.lift.bro.presentation.home

import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import com.lift.bro.BackupService
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.defaultSbdLifts
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Excercise
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.ui.LiftCardState
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
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
    ) { lifts, variations, sets ->
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
                            .map { it.key to it.value.maxOf { it.weight } },
                    )
                )
            }.sortedBy { it.state.lift.name.toLowerCase(Locale.current) }
                .toMutableList<DashboardListItem>().apply {
                    if (this.size == 0) {

                    } else if (this.size < 2) {
                        this.add(DashboardListItem.Ad)
                    } else {
                        this.add(2, DashboardListItem.Ad)
                    }
                }.toList(),
            excercises = sets.groupBy { it.date.toLocalDate() }.map { dateSetsEntry ->
                val date = dateSetsEntry.key
                dateSetsEntry.value.groupBy { it.variationId }.map { map ->
                    val variation = variations.firstOrNull { it.id == map.key }
                    variation?.let {
                        Excercise(
                            date = date,
                            variation = variation,
                            sets = map.value
                        )
                    }
                }
            }.flatten().filterNotNull()
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