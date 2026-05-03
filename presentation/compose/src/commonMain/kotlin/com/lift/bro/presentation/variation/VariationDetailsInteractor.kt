package com.lift.bro.presentation.variation

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator

typealias MovementDetailsInteractor = Interactor<MovementDetailsState, MovementDetailsEvent>

@Serializable
data class MovementDetailsState(
    val movement: Movement,
    val cards: List<MovementDetailsCard> = emptyList(),
)

@Serializable
data class MovementDetailsCard(
    val title: String,
    val sets: List<LBSet>,
)

sealed interface MovementDetailsEvent {
    data object AddSetClicked: MovementDetailsEvent
    data class SetClicked(val setId: String): MovementDetailsEvent

    sealed interface UpdateMovement: MovementDetailsEvent {
        data class NameUpdated(val name: String): UpdateMovement
        data class NotesUpdated(val notes: String): UpdateMovement
        data object ToggleBodyWeight: UpdateMovement
    }
}

@Composable
fun rememberMovementDetailsInteractor(
    movementId: String,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): MovementDetailsInteractor =
    rememberInteractor(
        initialState = MovementDetailsState(
            Movement(
                id = movementId,
            )
        ),
        source = { state ->
            combine(
                dependencies.variationRepository.listen(movementId),
                dependencies.setRepository.listenAll(variationId = movementId)
            ) { movement, sets ->
                MovementDetailsState(
                    movement = movement ?: state.movement,
                    cards = sets.groupBy { it.date }.map {
                        MovementDetailsCard(
                            title = it.key.toString("EEEE, MM d"),
                            sets = it.value
                        )
                    }
                )
            }
        },
        sideEffects = listOf(
            SideEffect { _, state, event ->
                when (event) {
                    is MovementDetailsEvent.UpdateMovement -> {
                        when (event) {
                            is MovementDetailsEvent.UpdateMovement.NameUpdated -> {
                                state.movement.let { variation ->
                                    dependencies.variationRepository.save(
                                        variation.copy(name = event.name)
                                    )
                                }
                            }

                            is MovementDetailsEvent.UpdateMovement.NotesUpdated -> {
                                val currentVariation = state.movement
                                currentVariation.let {
                                    dependencies.database.variantDataSource.save(
                                        currentVariation.copy(notes = event.notes)
                                    )
                                }
                            }

                            MovementDetailsEvent.UpdateMovement.ToggleBodyWeight -> {
                                state.movement.let { variation ->
                                    dependencies.variationRepository.save(
                                        variation.copy(bodyWeight = variation.bodyWeight?.not() ?: true)
                                    )
                                }
                            }
                        }
                    }

                    MovementDetailsEvent.AddSetClicked -> {
                        navCoordinator.present(CreateSet())
                    }

                    is MovementDetailsEvent.SetClicked -> {
                        navCoordinator.present(EditSet(event.setId))
                    }
                }
            }
        ),
        reducers = listOf(
            Reducer { state, event ->
                when (event) {
                    is MovementDetailsEvent.UpdateMovement -> {
                        when (event) {
                            is MovementDetailsEvent.UpdateMovement.NameUpdated -> state
                            MovementDetailsEvent.UpdateMovement.ToggleBodyWeight -> state
                            is MovementDetailsEvent.UpdateMovement.NotesUpdated -> state.copy(
                                movement = state.movement.copy(
                                    notes = event.notes
                                )
                            )
                        }
                    }

                    MovementDetailsEvent.AddSetClicked -> state
                    is MovementDetailsEvent.SetClicked -> state
                }
            }
        )
    )
