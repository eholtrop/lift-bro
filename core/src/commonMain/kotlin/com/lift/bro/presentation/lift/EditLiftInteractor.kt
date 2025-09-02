package com.lift.bro.presentation.lift

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
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
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.Serializable

@Serializable
data class EditLiftState(
    val id: String?,
    val name: String = "",
    val liftColor: LiftColor? = null,
    val variations: List<Variation> = emptyList(),
) {

    val lift
        get() = Lift(
            id = id ?: uuid4().toString(),
            name = name,
            color = liftColor?.color?.value
        )

    val showDelete = name.isNotBlank() && variations.isNotEmpty() && liftColor != null
}


@Serializable
data class LiftColor(
    val a: Int,
    val r: Int,
    val g: Int,
    val b: Int,
)

val LiftColor.color: Color get() = Color(a, r, g, b)

sealed class EditLiftEvent {
    data class NameChanged(val name: String) : EditLiftEvent()
    data class VariationNameChanged(val variation: Variation, val name: String) : EditLiftEvent()
    data object AddVariation : EditLiftEvent()
    data class VariationRemoved(val variation: Variation) : EditLiftEvent()

    data object DeleteLift : EditLiftEvent()
}

val EditLiftReducer = Reducer<EditLiftState, EditLiftEvent> { state, event ->
    Log.d(message = event.toString())
    when (event) {
        is EditLiftEvent.NameChanged -> state.copy(name = event.name)
        is EditLiftEvent.VariationNameChanged -> state.copy(
            variations = state.variations.map {
                if (it.id == event.variation.id) {
                    it.copy(name = event.name)
                } else {
                    it
                }
            },
        )

        EditLiftEvent.AddVariation -> state.copy(
            variations = listOf(Variation()) + state.variations
        )

        is EditLiftEvent.VariationRemoved -> state.copy(
            variations = state.variations.filter { it.id != event.variation.id },
        )

        EditLiftEvent.DeleteLift -> state
    }
}

fun editLiftSideEffects(
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    variationRepository: IVariationRepository = dependencies.database.variantDataSource,
    setRepository: SetDataSource = dependencies.database.setDataSource,
): SideEffect<EditLiftState, EditLiftEvent> = { state: EditLiftState, event: EditLiftEvent ->
    Log.d(message = event.toString())
    when (event) {
        EditLiftEvent.DeleteLift -> {
            if (state.id != null) {
                liftRepository.delete(state.id)
                state.variations.forEach {
                    variationRepository.delete(it.id)
                    setRepository.deleteAll(it.id)
                }
            }
        }

        EditLiftEvent.AddVariation -> {
            dependencies.database.variantDataSource.save(
                Variation(lift = state.lift)
            )
        }

        is EditLiftEvent.NameChanged -> {
            Log.d(message = state.lift.toString())
            dependencies.database.liftDataSource.save(state.lift.copy(name = event.name))
        }

        is EditLiftEvent.VariationRemoved -> {
            dependencies.database.variantDataSource.delete(event.variation.id)
            dependencies.database.setDataSource.deleteAll(event.variation.id)
        }

        is EditLiftEvent.VariationNameChanged -> {
            dependencies.database.variantDataSource.save(
                event.variation.copy(name = event.name)
            )
        }
    }
}

@Composable
fun rememberCreateLiftInteractor(
): Interactor<EditLiftState, EditLiftEvent> {
    val id = uuid4().toString()
    return rememberInteractor(
        initialState = EditLiftState(id = id),
        source = combine(
            dependencies.database.liftDataSource.get(id),
            dependencies.database.variantDataSource.listenAll(id),
        ) { lift, variations ->
            Log.d(message = lift.toString())
            EditLiftState(
                id = id,
                name = lift?.name ?: "",
                variations = variations.sortedBy { it.name?.toLowerCase(Locale.current) }
                    .sortedBy { !it.favourite }
                    .sortedBy { !it.name.isNullOrBlank() },
            )
        },
        reducers = listOf(EditLiftReducer),
        sideEffects = listOf(editLiftSideEffects())
    )
}

@Composable
fun rememberEditLiftInteractor(
    liftId: String,
): Interactor<EditLiftState, EditLiftEvent> {
    val thisId = liftId
    return rememberInteractor(
        initialState = EditLiftState(id = thisId),
        source = combine(
            dependencies.database.liftDataSource.get(thisId),
            dependencies.database.variantDataSource.listenAll(thisId),
        ) { lift, variations ->
            Log.d(message = lift.toString())
            EditLiftState(
                id = lift?.id,
                name = lift?.name ?: "",
                variations = variations.sortedBy { it.name?.toLowerCase(Locale.current) }
                    .sortedBy { !it.favourite }
                    .sortedBy { !it.name.isNullOrBlank() },
            )
        },
        reducers = listOf(EditLiftReducer),
        sideEffects = listOf(editLiftSideEffects())
    )
}