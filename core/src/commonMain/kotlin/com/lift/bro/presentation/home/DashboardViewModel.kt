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
            liftCards = lifts.map { lift ->
                val liftVariations = variations.filter { it.lift?.id == lift.id }
                val liftSets =
                    sets.filter { set -> liftVariations.any { set.variationId == it.id } }
                LiftCardState(
                    lift = lift,
                    values = liftSets.groupBy { it.date.toLocalDate() }
                        .map { it.key to it.value.maxOf { it.weight } },
                )
            }.sortedBy { it.lift.name.toLowerCase(Locale.current) },
            excercises = sets.groupBy { it.date.toLocalDate() }.map { dateSetsEntry ->
                val variation = variations
                    .firstOrNull { variation ->
                        dateSetsEntry.value.any { set -> set.variationId == variation.id }
                    }

                variation?.let {
                    Excercise(
                        date = dateSetsEntry.key,
                        variation = it,
                        sets = dateSetsEntry.value,
                    )
                }
            }.filterNotNull()
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