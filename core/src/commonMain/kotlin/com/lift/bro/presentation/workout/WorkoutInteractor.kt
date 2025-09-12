package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import com.benasher44.uuid.*
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.ISetDatasource
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.presentation.workout.CreateWorkoutEvent.*
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.serialization.Serializable

@Serializable
data class CreateWorkoutState(
    val id: String = uuid4().toString(),
    val date: LocalDate,
    val warmup: String? = null,
    val exercises: List<ExerciseItem> = emptyList(),
    val finisher: String? = null,
    val notes: String = "",
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
        val lastSet: LBSet,
    ): VariationItem
}

sealed class CreateWorkoutEvent {
    data class UpdateNotes(val notes: String): CreateWorkoutEvent()
    data class AddExercise(val variation: Variation): CreateWorkoutEvent()
    data class AddSuperSet(val exercise: ExerciseItem, val variation: Variation): CreateWorkoutEvent()
    data class UpdateFinisher(val finisher: String): CreateWorkoutEvent()
    data class UpdateWarmup(val warmup: String): CreateWorkoutEvent()
    data class DuplicateSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteSet(val set: LBSet): CreateWorkoutEvent()
    data class DeleteExercise(val exercise: ExerciseItem): CreateWorkoutEvent()

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
                WorkoutRepository(dependencies.database).get(date),
                dependencies.database.logDataSource.getByDate(date).flowToOneOrNull(),
            ) { workout, log ->
                CreateWorkoutState(
                    id = workout.id,
                    date = workout.date,
                    exercises = workout.exercises.map { exercise ->
                        ExerciseItem(
                            id = exercise.id,
                            variations = exercise.variationSets.map { variationSets ->
                                if (variationSets.sets.isEmpty()) {
                                    VariationItem.WithoutSets(
                                        id = variationSets.id,
                                        variation = variationSets.variation,
                                        lastSet = dependencies.database.setDataSource.getAll(
                                            variationSets.variation.id,
                                            1
                                        ).first()
                                    )
                                } else {
                                    VariationItem.WithSets(
                                        id = variationSets.id,
                                        variation = variationSets.variation,
                                        sets = variationSets.sets
                                    )
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
    }
}

fun workoutSideEffects(
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    setRepository: ISetDatasource = dependencies.setRepository,
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
            val newId = uuid4().toString()
            dependencies.database.exerciseDataSource.addExercise(
                workoutId = state.id,
                exerciseId = newId
            )

            dependencies.database.exerciseDataSource.addVariation(
                exerciseId = newId,
                variationId = event.variation.id
            )
        }

        is DeleteExercise -> {
            dependencies.database.exerciseDataSource.delete(event.exercise.id)
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

            dependencies.database.exerciseDataSource.removeVariaiton(
                exerciseVariationId = event.exerciseVariation.id
            )

            state.exercises.forEach { exercise ->
                if (exercise.variations.isEmpty()) {
                    dependencies.database.exerciseDataSource.delete(exercise.id)
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