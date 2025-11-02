package com.lift.bro.presentation.set

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.Sorting
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.utils.fullName
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class EditSetMaxPercentageState(
    val percentage: Int,
    val variationName: String,
)

@Serializable
data class EditSetState(
    val id: String?,
    val variation: Variation? = null,
    val weight: Double? = null,
    val reps: Long? = null,
    val eccentric: Long? = 3,
    val isometric: Long? = 1,
    val concentric: Long? = 1,
    val date: Instant = Clock.System.now(),
    val notes: String = "",
    val rpe: Int? = null,
    val variationMaxPercentage: EditSetMaxPercentageState? = null,
    val liftMaxPercentage: EditSetMaxPercentageState? = null,
) {
    val saveEnabled: Boolean get() = variation != null && reps != null && eccentric != null && isometric != null && concentric != null && weight != null
}

sealed interface EditSetEvent {
    data class VariationSelected(val variationId: String): EditSetEvent

    data class DateSelected(val date: Instant): EditSetEvent

    data object DeleteSetClicked: EditSetEvent

    data class RepChanged(val reps: Long?): EditSetEvent

    data class WeightChanged(val weight: Double?): EditSetEvent

    data class RpeChanged(val rpe: Int?): EditSetEvent

    data class EccChanged(val ecc: Int?): EditSetEvent

    data class IsoChanged(val iso: Int?): EditSetEvent

    data class ConChanged(val con: Int?): EditSetEvent

    data class NotesChanged(val notes: String): EditSetEvent
}

@Composable
fun rememberEditSetInteractor(
    setId: String,
    setRepository: ISetRepository = dependencies.setRepository,
    variationRepository: IVariationRepository = dependencies.variationRepository,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<EditSetState?, EditSetEvent> = rememberInteractor(
    initialState = null,
    source = {
        setRepository.listen(setId)
            .flatMapLatest { set ->
                if (set == null) {
                    flow {
                        emit(EditSetState(id = setId))
                    }
                } else {
                    variationRepository.listen(set.variationId)
                        .flatMapLatest { variation ->
                            combine(
                                setRepository.listenAll(
                                    variationId = variation?.id,
                                    limit = 1,
                                    sorting = Sorting.weight
                                ).map { it.firstOrNull() },
                                setRepository.listenAllForLift(
                                    liftId = variation?.lift?.id ?: "",
                                    limit = 1,
                                    sorting = Sorting.weight
                                ).map { it.firstOrNull() }
                            ) { maxVariation, maxLift ->
                                set.toUiState(
                                    variation = variation,
                                    maxVariationSet = maxVariation,
                                    maxLiftSet = if (maxLift?.variationId != maxVariation?.variationId) maxLift else null
                                )
                            }
                        }
                }
            }
    },
    reducers = reducers,
    sideEffects = sideEffects + { _, event -> if (event is EditSetEvent.DeleteSetClicked) navCoordinator.onBackPressed() }
)

@Composable
fun rememberCreateSetInteractor(
    variationId: String?,
    date: Instant?,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<EditSetState?, EditSetEvent> {
    val id = uuid4().toString()
    return rememberInteractor(
        initialState = null,
        source = {
            dependencies.setRepository.listen(id)
                .flatMapLatest { set ->
                    dependencies.variationRepository.listen(set?.variationId ?: variationId ?: "").map { variation ->
                        set?.toUiState(variation, null, null) ?: EditSetState(
                            id = id,
                            date = date ?: Clock.System.now(),
                            variation = variation
                        )
                    }
                }
        },
        sideEffects = sideEffects + { _, event -> if (event is EditSetEvent.DeleteSetClicked) navCoordinator.onBackPressed() },
        reducers = reducers,
    )
}

private val reducers: List<Reducer<EditSetState?, EditSetEvent>> = listOf(
    Reducer { state, event ->
        when (event) {
            is EditSetEvent.WeightChanged -> state?.copy(weight = event.weight)
            is EditSetEvent.RepChanged -> state?.copy(reps = event.reps)
            is EditSetEvent.RpeChanged -> state?.copy(rpe = event.rpe)
            is EditSetEvent.EccChanged -> state?.copy(eccentric = event.ecc?.toLong())
            is EditSetEvent.IsoChanged -> state?.copy(isometric = event.iso?.toLong())
            is EditSetEvent.ConChanged -> state?.copy(concentric = event.con?.toLong())
            is EditSetEvent.NotesChanged -> state?.copy(notes = event.notes)
            is EditSetEvent.VariationSelected -> state?.copy(variation = Variation(event.variationId))
            is EditSetEvent.DateSelected -> state?.copy(date = event.date)
            EditSetEvent.DeleteSetClicked -> state
        }
    }
)

private val sideEffects: List<SideEffect<EditSetState?, EditSetEvent>> = listOf { state, event ->
    if (event is EditSetEvent.DeleteSetClicked) {
        state?.toDomain()?.let {
            dependencies.setRepository.delete(it)
        }
    }

    state?.let {
        when (event) {
            is EditSetEvent.ConChanged ->
                state.copy(concentric = event.con?.toLong()).toDomain()

            is EditSetEvent.EccChanged -> {
                state.copy(eccentric = event.ecc?.toLong()).toDomain()
            }

            is EditSetEvent.IsoChanged -> {
                state.copy(isometric = event.iso?.toLong()).toDomain()
            }

            is EditSetEvent.DateSelected -> {
                state.copy(date = event.date).toDomain()
            }

            is EditSetEvent.NotesChanged -> {
                state.copy(notes = event.notes).toDomain()
            }

            is EditSetEvent.RepChanged ->
                state.copy(reps = event.reps).toDomain()


            is EditSetEvent.RpeChanged ->
                state.copy(rpe = event.rpe).toDomain()


            is EditSetEvent.VariationSelected ->
                state.copy(
                    variation = Variation(
                        event.variationId
                    )
                ).toDomain()


            is EditSetEvent.WeightChanged -> state.copy(weight = event.weight).toDomain()

            else -> null
        }?.also {
            if (state.saveEnabled) {
                dependencies.setRepository.save(it)
            }
        }
    }
}


private suspend fun LBSet.toUiState(
    variation: Variation?,
    maxVariationSet: LBSet?,
    maxLiftSet: LBSet?,
) = EditSetState(
    id = this.id,
    variation = variation,
    weight = this.weight,
    reps = this.reps,
    eccentric = this.tempo.down,
    isometric = this.tempo.hold,
    concentric = this.tempo.up,
    date = this.date,
    notes = this.notes,
    rpe = this.rpe,
    variationMaxPercentage = maxVariationSet?.let {
        EditSetMaxPercentageState(
            percentage = ((this.weight / maxVariationSet.weight) * 100).toInt(),
            variationName = variation?.fullName ?: ""
        )
    },
    liftMaxPercentage = maxLiftSet?.let {
        EditSetMaxPercentageState(
            percentage = ((this.weight / maxLiftSet.weight) * 100).toInt(),
            variationName = dependencies.liftRepository.get(variation?.lift?.id).firstOrNull()?.name ?: ""
        )
    }
)

private fun EditSetState.toDomain(): LBSet? =
    if (this.id != null && variation != null && reps != null && eccentric != null && isometric != null && concentric != null && weight != null) {
        LBSet(
            id = this.id,
            variationId = this.variation.id,
            weight = this.weight,
            reps = this.reps,
            tempo = Tempo
                (
                down = this.eccentric,
                hold = this.isometric,
                up = this.concentric
            ),
            date = this.date,
            notes = this.notes,
            rpe = this.rpe,
        )
    } else {
        null
    }
