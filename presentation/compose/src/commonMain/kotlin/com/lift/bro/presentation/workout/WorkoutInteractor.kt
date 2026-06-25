package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.di.dependencies
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.ApplicationScope
import com.lift.bro.presentation.workout.CreateWorkoutEvent.AddExercise
import com.lift.bro.presentation.workout.CreateWorkoutEvent.AddSuperSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteExercise
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteExerciseSection
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DuplicateSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateFinisher
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateNotes
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateWarmup
import com.lift.bro.ui.calendar.today
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.ktx.datetime.toLocalDate
import kotlin.collections.emptyList
import kotlin.time.Clock

@Serializable
data class CreateWorkoutState(
    val id: String = uuid4().toString(),
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<ExerciseItem> = emptyList(),
    val finisher: String? = null,
    val notes: String = "",
    val recentWorkouts: List<Workout> = emptyList(),
    val recommendedWorkout: Workout? = null,
)

@Serializable
data class ExerciseItem(
    val id: String,
    val sections: List<ExerciseSectionItem> = emptyList(),
    val recommendedExercise: Exercise? = null,
)

@Serializable
data class ExerciseSectionItem(
    val id: String,
    val sets: List<ExerciseSectionSet> = emptyList(),
    val recommendedSection: Section? = null,
)

@Serializable
data class ExerciseSectionSet(
    val set: LBSet,
    val movement: Movement,
    val recommended: Boolean,
)

sealed class CreateWorkoutEvent {
    data class UpdateNotes(val notes: String): CreateWorkoutEvent()
    data class AddExercise(val variation: Movement): CreateWorkoutEvent()
    data class AddSuperSet(val exercise: ExerciseItem, val variation: Movement):
        CreateWorkoutEvent()

    data class UpdateFinisher(val finisher: String): CreateWorkoutEvent()
    data class UpdateWarmup(val warmup: String): CreateWorkoutEvent()
    data class DuplicateSet(val set: LBSet, val forceToday: Boolean = false): CreateWorkoutEvent()
    data class DeleteSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteExercise(val exercise: ExerciseItem): CreateWorkoutEvent()

    data class CopyWorkout(val workout: Workout): CreateWorkoutEvent()

    data class DeleteExerciseSection(val exerciseSection: ExerciseSectionItem):
        CreateWorkoutEvent()
}

@Composable
fun rememberWorkoutInteractor(
    date: LocalDate,
): Interactor<CreateWorkoutState, CreateWorkoutEvent> =
    rememberInteractor(
        initialState = CreateWorkoutState(date = date),
        source = { og ->
            combine(
                dependencies.workoutRepository.get(date)
                    .map {
                        it ?: Workout(
                            id = uuid4().toString(),
                            date = date,
                            exercises = emptyList(),
                        )
                    },
                dependencies.workoutRepository.getAll(limit = 10),
                dependencies.database.logDataSource.getByDate(date).flowToOneOrNull(),
            ) { workout, workouts, log ->
                CreateWorkoutState(
                    id = workout.id,
                    date = workout.date,
                    recentWorkouts = workouts.filter { it.exercises.isNotEmpty() },
                    exercises = workout.exercises.map { exercise ->
                        ExerciseItem(
                            id = exercise.id,
                            sections = exercise.sections.map { section ->
                                ExerciseSectionItem(
                                    id = section.id,
                                    sets = section.movementSets.map { it.toItem(false) },
                                )
                            }
                        )
                    },
                    notes = log?.notes ?: "",
                    finisher = workout.finisher,
                    warmup = workout.warmup,
                )
            }
        },
        reducers = listOf(WorkoutReducer),
        sideEffects = listOf(workoutSideEffects())
    )

private fun Pair<Movement, LBSet>.toItem(recommended: Boolean): ExerciseSectionSet = ExerciseSectionSet(
    set = this.second,
    movement = this.first,
    recommended = recommended
)

val WorkoutReducer: Reducer<CreateWorkoutState, CreateWorkoutEvent> = Reducer { state, event ->
    when (event) {
        is AddExercise -> state.copy(
            exercises = state.exercises + ExerciseItem(
                id = uuid4().toString(),
                sections = listOf(
                    ExerciseSectionItem(
                        id = uuid4().toString(),
                    )
                )
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
        is DeleteExercise -> state.copy(exercises = state.exercises - event.exercise)
        is AddSuperSet -> state
        is DeleteExerciseSection -> state.copy(
            exercises = state.exercises.map {
                it.copy(sections = it.sections - event.exerciseSection)
            }
        )

        is CreateWorkoutEvent.CopyWorkout -> state.copy(
            recommendedWorkout = event.workout
        )
    }
}

fun workoutSideEffects(
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    setRepository: ISetRepository = dependencies.setRepository,
    liftLogRepository: LiftingLogQueries = dependencies.database.logDataSource,
): SideEffect<CreateWorkoutState, CreateWorkoutEvent> = SideEffect { _, state, event ->
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

        is CreateWorkoutEvent.CopyWorkout -> {
            ApplicationScope.launch {
                with(dependencies.workoutRepository) {
                    val workoutId = uuid4().toString()
                    save(
                        event.workout.copy(
                            id = workoutId,
                            date = state.date,
                            exercises = event.workout.exercises.map { exercise ->
                                val exerciseId = uuid4().toString()
                                exercise.copy(
                                    id = exerciseId,
                                    workoutId = workoutId,
                                    sections = exercise.sections.map {
                                        it.copy(
                                            id = uuid4().toString(),
                                            exerciseId = exerciseId,
                                        )
                                    }
                                )
                            }
                        )
                    )
                }
            }
        }

        is DuplicateSet -> {
            ApplicationScope.launch {
                setRepository.save(
                    lbSet = event.set.copy(
                        id = uuid4().toString(),
                        date = if (event.set.date.toLocalDate() != today && !event.forceToday) {
                            event.set.date.plus(1, DateTimeUnit.SECOND)
                        } else {
                            Clock.System
                                .now()
                        },
                    )
                )
            }
        }

        is DeleteSet -> {
            setRepository.delete(
                lbSet = event.set
            )
        }

        is AddExercise -> {
            ApplicationScope.launch {
                with(dependencies.workoutRepository) {
                    save(state.toWorkout())
                }
            }
        }

        is DeleteExercise -> {
            dependencies.exerciseRepository.delete(event.exercise.id)
        }

        is DeleteExerciseSection -> {
            dependencies.exerciseRepository.delete(
                section = Section(id = event.exerciseSection.id, exerciseId = ""),
                cascading = true,
            )
            state.exercises.filter { it.sections.isEmpty() }.forEach { exercise ->
                dependencies.exerciseRepository.delete(
                    id = exercise.id
                )
            }
        }

        is AddSuperSet -> {
            dependencies.exerciseRepository.save(
                section = Section(
                    exerciseId = event.exercise.id,
                    recommendedSets = emptyList(),
                )
            )
        }
    }

    if (state.finisher == null && state.warmup == null && state.exercises.isEmpty()) {
        workoutRepository.delete(state.toWorkout())
    }
}

private fun CreateWorkoutState.toWorkout(): Workout = Workout(
    id = this.id,
    date = this.date,
    warmup = this.warmup,
    exercises = this.exercises.map { exercise ->
        Exercise(
            id = exercise.id,
            workoutId = this.id,
            sections = exercise.sections.map { section ->
                Section(
                    id = section.id,
                    exerciseId = exercise.id,
                    sets = section.sets.map { it.set },
                    recommendedSets = section.sets.filter { it.recommended }.map { it.set },
                )
            }
        )
    },
    finisher = this.finisher
)
