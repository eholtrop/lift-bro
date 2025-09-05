package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.*
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.workout.CreateWorkoutEvent.*
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
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
    data class UpdateNotes(val notes: String): CreateWorkoutEvent()
    data class AddExercise(val variation: Variation): CreateWorkoutEvent()
    data class UpdateFinisher(val finisher: String): CreateWorkoutEvent()
    data class UpdateWarmup(val warmup: String): CreateWorkoutEvent()
    data class DuplicateSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteSet(val set: LBSet): CreateWorkoutEvent()
}

@Composable
fun rememberWorkoutInteractor(
    date: LocalDate,
): Interactor<CreateWorkoutState, CreateWorkoutEvent> =
    rememberInteractor(
        initialState = CreateWorkoutState(date = date),
        source = { state ->
            combine(
                WorkoutRepository(dependencies.database).get(date),
                dependencies.database.logDataSource.getByDate(date).asFlow()
                    .mapToOneOrNull(Dispatchers.IO),
            ) { workout, log ->
                CreateWorkoutState(
                    id = workout.id,
                    date = workout.date,
                    exercises = workout.exercises,
                    notes = log?.notes ?: "",
                    finisher = workout.finisher,
                    warmup = workout.warmup,
                )
            }
        },
        reducers = listOf(WorkoutReducer),
        sideEffects = listOf(workoutSideEffects())
    )

val WorkoutReducer: Reducer<CreateWorkoutState, CreateWorkoutEvent> = Reducer { state, event ->
    when (event) {
        is AddExercise -> state.copy(
            exercises = state.exercises + Exercise(
                sets = emptyList(),
                variation = event.variation
            )
        )

        is UpdateNotes -> {
            state.copy(notes = event.notes)
        }

        is UpdateFinisher -> {
            state.copy(finisher = event.finisher)
        }

        is UpdateWarmup -> {
            state.copy(warmup = event.warmup)
        }

        is DuplicateSet -> state
        is DeleteSet -> state
    }
}

fun workoutSideEffects(
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    liftLogRepository: LiftingLogQueries = dependencies.database.logDataSource,
): SideEffect<CreateWorkoutState, CreateWorkoutEvent> = { state, event ->
    when (event) {

        is UpdateNotes -> {
            val log = liftLogRepository.getByDate(state.date).executeAsOneOrNull()?.copy(
                notes = event.notes
            )
            liftLogRepository.save(
                id = log?.id ?: uuid4().toString(),
                date = state.date,
                notes = event.notes,
                vibe_check = log?.vibe_check
            )
        }

        is UpdateFinisher -> {
            workoutRepository.save(
                state.copy(finisher = event.finisher).toWorkout()
            )
        }

        is UpdateWarmup -> {
            workoutRepository.save(
                state.copy(warmup = event.warmup).toWorkout()
            )
        }

        is DuplicateSet -> {
            setRepository.save(
                lbSet = event.set.copy(id = uuid4().toString())
            )
        }

        is DeleteSet -> {
            setRepository.delete(
                lbSet = event.set
            )
        }

        is AddExercise -> {}
    }
}

private fun CreateWorkoutState.toWorkout(): Workout = Workout(
    id = this.id,
    date = this.date,
    warmup = this.warmup,
    exercises = this.exercises,
    finisher = this.finisher
)