package com.lift.bro.presentation.variation

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator

@Serializable
data class VariationDetailsState(
    val variation: Variation,
    val notes: String? = null,
    val cards: List<VariationDetailCard> = emptyList(),
)

@Serializable
data class VariationDetailCard(
    val title: String,
    val sets: List<LBSet>,
)

sealed interface VariationDetailsEvent {
    data class NotesUpdated(val notes: String): VariationDetailsEvent
    data class SetClicked(val setId: String): VariationDetailsEvent
    data object AddSetClicked: VariationDetailsEvent

    data class NameUpdated(val name: String): VariationDetailsEvent

    data object ToggleBodyWeight: VariationDetailsEvent
}

@Composable
fun rememberVariationDetailInteractor(
    variationId: String,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<VariationDetailsState, VariationDetailsEvent> =
    rememberInteractor(
        initialState = VariationDetailsState(Variation()),
        source = {
            combine(
                dependencies.database.variantDataSource.listen(variationId),
                dependencies.database.setDataSource.listenAllForVariation(variationId)
            ) { variation, sets ->
                VariationDetailsState(
                    variation = variation!!,
                    cards = sets.groupBy { it.date }.map {
                        VariationDetailCard(
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
                    VariationDetailsEvent.AddSetClicked -> {
                        navCoordinator.present(CreateSet())
                    }

                    is VariationDetailsEvent.NotesUpdated -> {
                        if (event.notes.isNotBlank() && state.variation.notes.isNullOrBlank()) {
                            dependencies.database.variantDataSource.save(
                                state.variation.copy(notes = event.notes)
                            )
                        }
                    }

                    is VariationDetailsEvent.SetClicked -> {
                        navCoordinator.present(EditSet(event.setId))
                    }

                    is VariationDetailsEvent.NameUpdated -> {
                        dependencies.variationRepository.save(
                            state.variation.copy(name = event.name)
                        )
                    }

                    VariationDetailsEvent.ToggleBodyWeight -> {
                        dependencies.variationRepository.save(
                            state.variation.copy(bodyWeight = state.variation.bodyWeight?.not() ?: true)
                        )
                    }
                }
            }
        ),
        reducers = listOf(
            Reducer { state, event ->
                when (event) {
                    is VariationDetailsEvent.NotesUpdated -> state.copy(
                        variation = state.variation.copy(
                            notes = event.notes
                        )
                    )

                    VariationDetailsEvent.AddSetClicked -> state
                    is VariationDetailsEvent.SetClicked -> state
                    is VariationDetailsEvent.NameUpdated -> state
                    VariationDetailsEvent.ToggleBodyWeight -> state
                }
            }
        )
    )
