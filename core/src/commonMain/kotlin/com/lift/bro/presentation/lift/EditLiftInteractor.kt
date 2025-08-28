package com.lift.bro.presentation.lift

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LiftDataSource
import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.serialization.Serializable

@Serializable
data class EditLiftState(
    val id: String?,
    val name: String = "",
    val variations: List<EditLiftVariation> = emptyList(),
)

@Serializable
data class EditLiftVariation(
    val variation: Variation,
    val shouldShowDeleteWarning: Boolean = false,
)

sealed class EditLiftEvent {
    data class NameChanged(val name: String) : EditLiftEvent()
    data class VariationNameChanged(val variation: Variation, val name: String) : EditLiftEvent()
    data object AddVariation : EditLiftEvent()
    data class VariationRemoved(val variation: Variation) : EditLiftEvent()

    data object DeleteLift : EditLiftEvent()
    data object Save : EditLiftEvent()
}

val EditLiftReducer = Reducer<EditLiftState, EditLiftEvent> { state, event ->
    when (event) {
        is EditLiftEvent.NameChanged -> state.copy(name = event.name)
        is EditLiftEvent.VariationNameChanged -> state.copy(variations = state.variations.map {
            if (it.variation.id == event.variation.id) {
                it.copy(variation = it.variation.copy(name = event.name))
            } else {
                it
            }
        })

        EditLiftEvent.AddVariation -> state.copy(
            variations = listOf(EditLiftVariation(Variation())) + state.variations
        )

        is EditLiftEvent.VariationRemoved -> state.copy(variations = state.variations.filter { it.variation != event.variation })
        EditLiftEvent.Save -> state
        EditLiftEvent.DeleteLift -> state
    }
}

fun editLiftSideEffects(
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.database.variantDataSource,
    setRepository: SetDataSource = dependencies.database.setDataSource,
): SideEffect<EditLiftState, EditLiftEvent> = { state: EditLiftState, event: EditLiftEvent ->
    when (event) {
        EditLiftEvent.Save -> {
            dependencies.database.liftDataSource.save(
                Lift(
                    id = state.id ?: uuid4().toString(),
                    name = state.name
                )
            )
            state.variations.filter { it.variation.name != null }.forEach {
                dependencies.database.variantDataSource.save(it.variation)
            }
        }

        EditLiftEvent.DeleteLift -> {
            if (state.id != null) {
                liftRepository.delete(state.id)
                state.variations.forEach {
                    variationRepository.delete(it.variation.id)
                    setRepository.deleteAll(it.variation.id)
                }
            }
        }

        EditLiftEvent.AddVariation -> {}
        is EditLiftEvent.NameChanged -> {}
        is EditLiftEvent.VariationRemoved -> {
            dependencies.database.variantDataSource.delete(event.variation.id)
            dependencies.database.setDataSource.deleteAll(event.variation.id)
        }

        is EditLiftEvent.VariationNameChanged -> {}
    }
}

@Composable
fun rememberEditLiftInteractor(
    liftId: String?,
): Interactor<EditLiftState, EditLiftEvent> = rememberInteractor(
    initialState = EditLiftState(id = liftId ?: ""),
    source = combine(
        dependencies.database.liftDataSource.get(liftId).filterNotNull(),
        dependencies.database.variantDataSource.listenAll(liftId ?: ""),
    ) { lift, variations ->
        EditLiftState(
            id = lift.id,
            name = lift.name,
            variations = variations.map {
                EditLiftVariation(
                    it,
                    shouldShowDeleteWarning = it.eMax != null || it.oneRepMax != null
                )
            }
        )
    },
    stateResolver = { initial, source ->
        source.copy(
            variations = initial.variations.filter { !it.shouldShowDeleteWarning } + source.variations
        )
    },
    reducers = listOf(EditLiftReducer),
    sideEffects = listOf(editLiftSideEffects())
)