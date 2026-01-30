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
import com.lift.bro.utils.fullName
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.logging.Log
import tv.dpal.logging.d
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator
import kotlin.math.max

@Serializable
data class EditSetMaxPercentageState(
    val percentage: Int,
    val variationName: String,
)

@Serializable
data class EditSetState(
    val id: String?,
    val weight: Double? = null,
    val reps: Long? = null,
    val rpe: Int? = null,
    val defaultRpe: Int? = null,
    val tempo: TempoState = TempoState(),
    val mers: Int? = null,
    val eMax: Int? = null,
    val tMax: Int? = null,
    val notes: String = "",
    val totalWeightMoved: Double? = null,
    val date: Instant = Clock.System.now(),
    val variation: SetVariation? = null,
) {
    val saveEnabled: Boolean get() = variation != null && reps != null && tempo != null && weight != null
}

@Serializable
data class SetVariation(
    val variation: Variation,
    val variationMaxPercentage: EditSetMaxPercentageState? = null,
    val liftMaxPercentage: EditSetMaxPercentageState? = null,
)

@Serializable
data class TempoState(
    val ecc: Long? = 3,
    val iso: Long? = 1,
    val con: Long? = 1,
)

sealed interface EditSetEvent {
    data class VariationSelected(val variation: Variation): EditSetEvent

    data class DateSelected(val date: LocalDate): EditSetEvent

    data object DeleteSetClicked: EditSetEvent

    data class RepChanged(val reps: Long?): EditSetEvent

    data class WeightChanged(val weight: Double?): EditSetEvent

    data class RpeChanged(val rpe: Int?): EditSetEvent

    data class TempoChanged(val tempo: TempoState): EditSetEvent

    data class NotesChanged(val notes: String): EditSetEvent
}

@OptIn(ExperimentalCoroutinesApi::class)
private fun editSetSource(
    setId: String,
    date: Instant? = null,
    variationId: String? = null,
    setRepository: ISetRepository = dependencies.setRepository,
    variationRepository: IVariationRepository = dependencies.variationRepository,
) = setRepository.listen(setId)
    .map {
        it ?: LBSet(
            id = setId,
            date = date ?: Clock.System.now(),
            variationId = variationId ?: "",
        )
    }
    .flatMapLatest { set ->
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

@OptIn(ExperimentalCoroutinesApi::class)
@Composable
fun rememberEditSetInteractor(
    setId: String,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<EditSetState?, EditSetEvent> = rememberInteractor(
    initialState = null,
    source = { editSetSource(setId) },
    reducers = listOf(EditSetReducer),
    sideEffects = listOf(editSetSideEffects()) + listOf(
        SideEffect { _, _, event ->
            if (event is EditSetEvent.DeleteSetClicked) navCoordinator.onBackPressed()
        }
    )
)

@OptIn(ExperimentalCoroutinesApi::class)
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
            editSetSource(id, date, variationId)
        },
        sideEffects = listOf(editSetSideEffects()) + listOf(
            SideEffect { _, _, event -> if (event is EditSetEvent.DeleteSetClicked) navCoordinator.onBackPressed() }
        ),
        reducers = listOf(EditSetReducer),
    )
}

val EditSetReducer: Reducer<EditSetState?, EditSetEvent> = Reducer { state, event ->
    when (event) {
        is EditSetEvent.WeightChanged -> state?.copy(weight = event.weight)
        is EditSetEvent.RepChanged -> state?.copy(reps = event.reps)
        is EditSetEvent.RpeChanged -> state?.copy(rpe = event.rpe)
        is EditSetEvent.TempoChanged -> state?.copy(tempo = event.tempo)
        is EditSetEvent.NotesChanged -> state?.copy(notes = event.notes)
        is EditSetEvent.VariationSelected -> state!!.copy(
            variation = SetVariation(
                variation = event.variation,
                variationMaxPercentage = null,
                liftMaxPercentage = null
            )
        )

        is EditSetEvent.DateSelected -> state?.copy(
            date = event.date.atTime(
                state.date.toLocalDateTime(TimeZone.currentSystemDefault()).time
            ).toInstant(TimeZone.currentSystemDefault())
        )

        EditSetEvent.DeleteSetClicked -> state
    }
}

fun editSetSideEffects(
    setRepository: ISetRepository = dependencies.setRepository,
): SideEffect<EditSetState?, EditSetEvent> = SideEffect { _, state, event ->
    when (event) {
        is EditSetEvent.DeleteSetClicked -> {
            state?.toDomain()?.let {
                setRepository.delete(it)
            }
        }

        else -> {
            Log.d(message = "saving - $state")
            state?.toDomain()?.let {
                Log.d(message = "saving - $event")
                setRepository.save(it)
            }
        }
    }
}

internal suspend fun LBSet.toUiState(
    variation: Variation?,
    maxVariationSet: LBSet?,
    maxLiftSet: LBSet?,
) = EditSetState(
    id = this.id,
    variation = variation?.let {
        SetVariation(
            variation = Variation(id = this.variationId),
            variationMaxPercentage = maxVariationSet?.let {
                EditSetMaxPercentageState(
                    percentage = ((this.weight / max(maxVariationSet.weight, 1.0)) * 100).toInt(),
                    variationName = variation.fullName
                )
            },
            liftMaxPercentage = maxLiftSet?.let {
                EditSetMaxPercentageState(
                    percentage = ((this.weight / max(maxLiftSet.weight, 1.0)) * 100).toInt(),
                    variationName = dependencies.liftRepository.get(variation.lift?.id).firstOrNull()?.name ?: ""
                )
            }
        )
    },
    weight = this.weight,
    reps = this.reps,
    tempo = TempoState(
        ecc = this.tempo.down,
        iso = this.tempo.hold,
        con = this.tempo.up
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe,
    mers = this.mer,
    defaultRpe = null // maxVariationSet?.let { this.weight.div(it.weight).times(10).toInt() },

)

internal fun EditSetState.toDomain(): LBSet? =
    if (this.id != null && variation != null && reps != null && tempo.ecc != null && tempo.iso != null && tempo.con != null && weight != null) {
        LBSet(
            id = this.id,
            variationId = this.variation.variation.id,
            weight = this.weight,
            reps = this.reps,
            tempo = Tempo(
                down = this.tempo.ecc,
                hold = this.tempo.iso,
                up = this.tempo.con,
            ),
            date = this.date,
            notes = this.notes,
            rpe = this.rpe,
        )
    } else {
        null
    }
