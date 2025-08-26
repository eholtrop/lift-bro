package com.lift.bro.presentation.workout

import androidx.annotation.RestrictTo
import androidx.compose.ui.platform.LocalGraphicsContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.*
import com.lift.bro.data.SetDataSource
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.workout.CreateWorkoutEvent.AddExercise
import com.lift.bro.utils.debug
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
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
    val id: String = uuid4().toString(),
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<Exercise> = emptyList(),
    val finisher: String? = null,
    val notes: String = "",
)

sealed class CreateWorkoutEvent {
    data class UpdateNotes(val notes: String) : CreateWorkoutEvent()
    data class AddExercise(val variation: Variation) : CreateWorkoutEvent()

    data class UpdateFinisher(val finisher: String) : CreateWorkoutEvent()

    data class UpdateWarmup(val warmup: String) : CreateWorkoutEvent()
}

class CreateWorkoutViewModel(
    initialState: CreateWorkoutState,
    workoutRepository: WorkoutRepository = WorkoutRepository(dependencies.database),
    liftLogRepository: LiftingLogQueries = dependencies.database.logDataSource,
    coroutineScope: CoroutineScope
) {

    private val inputs: Channel<CreateWorkoutEvent> = Channel()

    fun handleEvent(event: CreateWorkoutEvent) {
        inputs.trySend(event)
    }

    val state: StateFlow<CreateWorkoutState> = combine(
        workoutRepository.get(initialState.date),
        liftLogRepository.getByDate(initialState.date).asFlow().mapToOneOrNull(Dispatchers.IO),
        flowOf(initialState),
    ) { workout, log, initialState ->
        CreateWorkoutState(
            id = workout.id,
            date = workout.date,
            exercises = workout.exercises,
            notes = log?.notes ?: "",
            finisher = workout.finisher,
            warmup = workout.warmup,
        )
    }
        .flatMapLatest { state ->
            inputs.receiveAsFlow()
                .scan(state) { state, event ->
                    when (event) {
                        is AddExercise -> state.copy(
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
                                id = log?.id ?: uuid4().toString(),
                                date = initialState.date,
                                notes = event.notes,
                                vibe_check = log?.vibe_check
                            )
                            state.copy(notes = event.notes)
                        }

                        is CreateWorkoutEvent.UpdateFinisher -> {
                            if (event.finisher.isNotBlank()) {
                                workoutRepository.save(
                                    state.copy(finisher = event.finisher).toWorkout()
                                )
                            }
                            state.copy(finisher = event.finisher)
                        }
                        is CreateWorkoutEvent.UpdateWarmup -> {
                            if (event.warmup.isNotBlank()) {
                                workoutRepository.save(
                                    state.copy(warmup = event.warmup).toWorkout()
                                )
                            }
                            state.copy(warmup = event.warmup)
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

private fun CreateWorkoutState.toWorkout(): Workout = Workout(
    id = this.id,
    date = this.date,
    warmup = this.warmup,
    exercises = this.exercises,
    finisher = this.finisher
)