package com.lift.bro.presentation.workout

import androidx.annotation.RestrictTo
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.SetDataSource
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Variation
import com.lift.bro.utils.debug
import com.lift.bro.utils.toLocalDate
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkoutState(
    val date: LocalDate,
    val warmup: String = "",
    val exercises: List<Exercise> = emptyList(),
    val finisher: String = "",
    val notes: String = "",
)

sealed class CreateWorkoutEvent {
    data class UpdateNotes(val notes: String) : CreateWorkoutEvent()
    data class AddExercise(val variation: Variation) : CreateWorkoutEvent()
}

class CreateWorkoutViewModel(
    initialState: CreateWorkoutState,
    setRepository: SetDataSource = dependencies.database.setDataSource,
    liftLogRepository: LiftingLogQueries = dependencies.database.logDataSource,
    coroutineScope: CoroutineScope
) {

    private val inputs: Channel<CreateWorkoutEvent> = Channel()

    fun handleEvent(event: CreateWorkoutEvent) {
        inputs.trySend(event)
    }

    val state: StateFlow<CreateWorkoutState> = combine(
        setRepository.listenAll(),
        liftLogRepository.getByDate(initialState.date).asFlow().mapToOneOrNull(Dispatchers.IO),
        flowOf(initialState),
    ) { sets, log, initialState ->
        CreateWorkoutState(
            date = initialState.date,
            exercises = sets.filter { it.date.toLocalDate() == initialState.date }
                .groupBy { it.variationId }
                .map { (id, sets) ->
                    Exercise(
                        sets = sets,
                        variation = dependencies.database.variantDataSource.get(id)!!
                    )
                },
            notes = log?.notes ?: ""
        )
    }
        .flatMapLatest { state ->
            inputs.receiveAsFlow()
                .scan(state) { state, event ->
                    when (event) {
                        is CreateWorkoutEvent.AddExercise -> state.copy(
                            exercises = state.exercises + Exercise(
                                sets = emptyList(),
                                variation = event.variation
                            )
                        )

                        is CreateWorkoutEvent.UpdateNotes -> {
                            val log =
                                liftLogRepository.getByDate(initialState.date).executeAsOneOrNull()
                                    ?.copy(
                                        notes = event.notes
                                    )
                            liftLogRepository.save(
                                id = log?.id ?: com.benasher44.uuid.uuid4().toString(),
                                date = initialState.date,
                                notes = event.notes,
                                vibe_check = log?.vibe_check
                            )
                            state.copy(notes = event.notes)
                        }
                    }
                }
        }
        .stateIn(
            coroutineScope,
            SharingStarted.WhileSubscribed(),
            initialState,
        )
}