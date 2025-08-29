package com.lift.bro.presentation.lift

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.workout.workoutCalendarSourceData
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.Destination.*
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.utils.combine
import com.lift.bro.utils.toColor
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.collections.listOf

@Serializable
data class LiftDetailsState(
    val liftName: String? = null,
    val liftColor: ULong? = null,
    val variations: List<VariationCardState> = emptyList(),
)

@Serializable
data class VariationCardState(
    val variation: Variation,
    val sets: List<LBSet>,
)

sealed interface LiftDetailsEvent {
    data class LiftColorChanged(val color: ULong) : LiftDetailsEvent
    data class VariationClicked(val variation: Variation) : LiftDetailsEvent
    data class SetClicked(val lbSet: LBSet) : LiftDetailsEvent

    data class ToggleFavourite(val variation: Variation) : LiftDetailsEvent

    data object AddSetClicked : LiftDetailsEvent

    data object EditLiftClicked : LiftDetailsEvent
}

@Composable
fun rememberLiftDetailsInteractor(
    liftId: String,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current
): Interactor<LiftDetailsState, LiftDetailsEvent> = rememberInteractor(
    initialState = LiftDetailsState(),
    source = combine(
        dependencies.database.liftDataSource.get(liftId),
        dependencies.database.variantDataSource.listenAll(liftId),
        dependencies.database.setDataSource.listenAllForLift(liftId)
            .map { it.groupBy { it.variationId } }
    ) { lift, variations, sets ->
        LiftDetailsState(
            liftName = lift?.name ?: "",
            liftColor = lift?.color,
            variations = variations.map {
                VariationCardState(
                    variation = it,
                    sets = sets[it.id] ?: emptyList()
                )
            },
        )
    },
    sideEffects = listOf { state, event ->
        when (event) {
            LiftDetailsEvent.AddSetClicked -> navCoordinator.present(
                EditSet(
                    liftId = liftId
                )
            )

            is LiftDetailsEvent.LiftColorChanged -> {
                dependencies.database.liftDataSource.save(
                    Lift(
                        id = liftId,
                        name = state.liftName ?: "",
                        color = event.color,
                    )
                )
            }

            is LiftDetailsEvent.SetClicked ->
                navCoordinator.present(
                    EditSet(
                        setId = event.lbSet.id
                    )
                )

            is LiftDetailsEvent.VariationClicked ->
                navCoordinator.present(
                    VariationDetails(
                        variationId = event.variation.id
                    )
                )

            is LiftDetailsEvent.EditLiftClicked ->
                navCoordinator.present(
                    EditLift(
                        liftId = liftId
                    )
                )

            is LiftDetailsEvent.ToggleFavourite -> {
                dependencies.database.variantDataSource.save(
                    event.variation.copy(
                        favourite = !event.variation.favourite
                    )
                )
            }
        }
    }
)