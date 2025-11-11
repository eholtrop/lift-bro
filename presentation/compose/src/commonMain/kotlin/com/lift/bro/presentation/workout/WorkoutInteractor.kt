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
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.ApplicationScope
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.workout.CreateWorkoutEvent.AddExercise
import com.lift.bro.presentation.workout.CreateWorkoutEvent.AddSuperSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteExercise
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DeleteVariation
import com.lift.bro.presentation.workout.CreateWorkoutEvent.DuplicateSet
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateFinisher
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateNotes
import com.lift.bro.presentation.workout.CreateWorkoutEvent.UpdateWarmup
import com.lift.bro.ui.today
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable
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
)

@Serializable
data class ExerciseItem(
    val id: String,
    val variations: List<VariationItem>,
)

@Serializable
sealed interface VariationItem {

    val id: String

    val variation: Variation

    @Serializable
    data class WithSets(
        override val id: String,
        override val variation: Variation,
        val sets: List<LBSet>,
    ): VariationItem

    @Serializable
    data class WithoutSets(
        override val id: String,
        override val variation: Variation,
        val lastSet: LBSet?,
    ): VariationItem
}

sealed class CreateWorkoutEvent {
    data class UpdateNotes(val notes: String): CreateWorkoutEvent()
    data class AddExercise(val variation: Variation): CreateWorkoutEvent()
    data class AddSuperSet(val exercise: ExerciseItem, val variation: Variation):
        CreateWorkoutEvent()

    data class UpdateFinisher(val finisher: String): CreateWorkoutEvent()
    data class UpdateWarmup(val warmup: String): CreateWorkoutEvent()
    data class DuplicateSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteExercise(val exercise: ExerciseItem): CreateWorkoutEvent()

    data class CopyWorkout(val workout: Workout): CreateWorkoutEvent()

    data class DeleteVariation(val exerciseVariation: VariationItem):
        CreateWorkoutEvent()
}

@Composable
fun rememberWorkoutInteractor(
    date: LocalDate,
): Interactor<CreateWorkoutState, CreateWorkoutEvent> =
    rememberInteractor(
        initialState = CreateWorkoutState(date = date),
        source = { state ->
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
                            variations = exercise.variationSets.map { variationSets ->
                                when {
                                    variationSets.sets.isEmpty() -> {
                                        VariationItem.WithoutSets(
                                            id = variationSets.id,
                                            variation = variationSets.variation,
                                            lastSet = dependencies.database.setDataSource.getAll(
                                                variationId = variationSets.variation.id,
                                                limit = 1
                                            ).firstOrNull()
                                        )
                                    }

                                    else -> {
                                        VariationItem.WithSets(
                                            id = variationSets.id,
                                            variation = variationSets.variation,
                                            sets = variationSets.sets
                                        )
                                    }
                                }
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

val WorkoutReducer: Reducer<CreateWorkoutState, CreateWorkoutEvent> = Reducer { state, event ->
    when (event) {
        is AddExercise -> state

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
        is DeleteVariation -> state.copy(exercises = state.exercises.map {
            it.copy(variations = it.variations - event.exerciseVariation)
        })

        is CreateWorkoutEvent.CopyWorkout -> state
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

        is CreateWorkoutEvent.CopyWorkout -> {
            ApplicationScope.launch {
                with(dependencies.workoutRepository) {
                    save(state.toWorkout())
                    event.workout.exercises.forEach { exercise ->
                        val newEid = uuid4().toString()

                        exercise.variationSets.forEach {
                            addVariation(
                                exerciseId = newEid,
                                variationId = it.variation.id
                            )
                        }
                        addExercise(
                            workoutId = state.id, exerciseId = newEid
                        )
                    }
                }
            }
        }

        is DuplicateSet -> {
            setRepository.save(
                lbSet = event.set.copy(
                    id = uuid4().toString(),
                    date = state.date.atStartOfDayIn(TimeZone.currentSystemDefault())
                )
            )
        }

        is DeleteSet -> {
            setRepository.delete(
                lbSet = event.set
            )
        }

        is AddExercise -> {
            ApplicationScope.launch {
                val newId = uuid4().toString()
                with(dependencies.workoutRepository) {
                    addVariation(newId, event.variation.id)
                    addExercise(state.id, newId)
                    save(state.toWorkout())
                }
            }
        }

        is DeleteExercise -> {
            dependencies.workoutRepository.deleteExercise(event.exercise.id)
        }

        is DeleteVariation -> {
            when (event.exerciseVariation) {
                is VariationItem.WithSets -> {
                    event.exerciseVariation.sets.forEach {
                        dependencies.setRepository.delete(it)
                    }
                }

                is VariationItem.WithoutSets -> {}
            }

            dependencies.workoutRepository.removeVariation(
                exerciseVariationId = event.exerciseVariation.id
            )

            state.exercises.forEach { exercise ->
                if (exercise.variations.isEmpty()) {
                    dependencies.workoutRepository.deleteExercise(exercise.id)
                }
            }
        }

        is AddSuperSet -> {
            dependencies.database.exerciseDataSource.addVariation(
                exerciseId = event.exercise.id,
                variationId = event.variation.id
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
    exercises = this.exercises.map {
        Exercise(
            id = it.id,
            workoutId = this.id,
            variationSets = it.variations.map { variation ->
                when (variation) {
                    is VariationItem.WithSets -> {
                        VariationSets(
                            id = variation.id,
                            variation = variation.variation,
                            sets = variation.sets
                        )
                    }

                    is VariationItem.WithoutSets -> {
                        VariationSets(
                            id = variation.id,
                            variation = variation.variation,
                            sets = emptyList()
                        )
                    }
                }
            }
        )
    },
    finisher = this.finisher
)
