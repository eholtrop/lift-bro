package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.uuid4
import com.lift.bro.data.SetDataSource
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.today
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutCalendarState(
    val workouts: List<Workout> = emptyList(),
    val date: LocalDate,
    val workout: Workout?,
    val unallocatedVariationSets: List<VariationSets> = emptyList(),
    val logs: List<LiftingLog> = emptyList(),
)

@Composable
fun rememberWorkoutCalendarInteractor(
    initialDate: LocalDate = today,
) = rememberInteractor(
    initialState = WorkoutCalendarState(
        date = initialDate,
        workout = null
    ),
    stateResolver = { initial, source ->
        source.copy(
            date = if (initial.date != source.date) initial.date else source.date,
            workout = if (initial.date != source.date) initial.workout else source.workout,
        )
    },
    source = { workoutCalendarSourceData() },
    reducers = listOf(WorkoutCalendarReducer),
    sideEffects = navigationSideEffects() + dataSideEffects(),
)

@Composable
private fun navigationSideEffects(
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> {
    return listOf { state, event ->
        when (event) {
            is WorkoutCalendarEvent.AddWorkoutClicked -> navCoordinator.present(
                Destination.CreateWorkout(event.onDate)
            )

            is WorkoutCalendarEvent.WorkoutClicked -> navCoordinator.present(
                Destination.CreateWorkout(event.workout.date)
            )

            else -> {}
        }
    }
}

private fun dataSideEffects(
): List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> {
    return listOf { state, event ->
        when (event) {
            is WorkoutCalendarEvent.AddWorkoutClicked -> {}
            is WorkoutCalendarEvent.AddToWorkout -> {
                val workout = dependencies.workoutRepository.get(event.date).first()

                if (workout != null) {
                    Log.d(message = "workout not null")
                    dependencies.exerciseRepository.save(
                        Exercise(
                            id = uuid4().toString(),
                            workoutId = workout.id,
                            variationSets = listOf(
                                VariationSets(
                                    id = uuid4().toString(),
                                    variation = event.variation,
                                    sets = emptyList(),
                                )
                            )
                        )
                    )
                    Log.d(message = "exercise saved")
                } else {
                    Log.d(message = "workout null")
                    dependencies.workoutRepository.save(
                        Workout(
                            id = state.workout?.id ?: uuid4().toString(),
                            date = event.date,
                            exercises = emptyList()
                        )
                    )
                    Log.d(message = "workout saved")
                    dependencies.exerciseRepository.save(
                        Exercise(
                            id = uuid4().toString(),
                            workoutId = uuid4().toString(),
                            variationSets = listOf(
                                VariationSets(
                                    id = uuid4().toString(),
                                    variation = event.variation,
                                    sets = emptyList(),
                                )
                            )
                        )
                    )
                    Log.d(message = "exercise saved")
                }
            }

            is WorkoutCalendarEvent.DateSelected -> {}
            is WorkoutCalendarEvent.LoadMonth -> {}
            is WorkoutCalendarEvent.WorkoutClicked -> {}
        }
    }
}

fun workoutCalendarSourceData(
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    logQueries: LiftingLogQueries = dependencies.database.logDataSource,
) = combine(
    workoutRepository.getAll(
        LocalDate(today.year, today.month, 1),
        LocalDate(today.year, today.month, 1)
            .plus(1, DateTimeUnit.MONTH),
    ),
    logQueries.getAll().asFlow().mapToList(Dispatchers.IO),
    GetGhostSetsForMonthUseCase(today.year, today.month)

) { workouts, logs, sets ->
    WorkoutCalendarState(
        workouts = workouts,
        logs = logs.map {
            LiftingLog(
                id = it.id,
                date = it.date,
                notes = it.notes ?: "",
                vibe = it.vibe_check?.toInt()
            )
        },
        date = today,
        unallocatedVariationSets = sets,
        workout = workouts.find { it.date == today }
    )
}

fun GetGhostSetsForMonthUseCase(
    year: Int,
    month: Month,
): Flow<List<VariationSets>> = combine(
    dependencies.database.setDataSource.listenAll(
        startDate = LocalDate(year, month, 1),
        endDate = LocalDate(today.year, today.month, 1)
            .plus(1, DateTimeUnit.MONTH)
    ),
    dependencies.database.exerciseQueries.getExercizeVariationsByDate(
        startDate = LocalDate(year, month, 1),
        endDate = LocalDate(today.year, today.month, 1)
            .plus(1, DateTimeUnit.MONTH)
    ).asFlow().mapToList(Dispatchers.IO),
    dependencies.variationRepository.listenAll(),
) { sets, exerciseVariations, allVariations ->

    sets.filter { set -> exerciseVariations.none { it.varationId == set.variationId } }
        .groupBy { it.variationId }
        .map { (id, sets) ->
            VariationSets(
                id = id,
                sets = sets,
                variation = allVariations.first { it.id == id }
            )
        }
}

sealed interface WorkoutCalendarEvent {
    data class AddWorkoutClicked(val onDate: LocalDate): WorkoutCalendarEvent
    data class WorkoutClicked(val workout: Workout): WorkoutCalendarEvent
    data class DateSelected(val date: LocalDate): WorkoutCalendarEvent
    data class AddToWorkout(val date: LocalDate, val variation: Variation): WorkoutCalendarEvent

    data class LoadMonth(val year: Int, val month: Month): WorkoutCalendarEvent
}

private val Month.daysIn: Int
    get() =
        when (this) {
            Month.JANUARY -> 31
            Month.FEBRUARY -> 28
            Month.MARCH -> 31
            Month.APRIL -> 30
            Month.MAY -> 31
            Month.JUNE -> 30
            Month.JULY -> 31
            Month.AUGUST -> 31
            Month.SEPTEMBER -> 30
            Month.OCTOBER -> 31
            Month.NOVEMBER -> 30
            Month.DECEMBER -> 31
            else -> 0
        }

val WorkoutCalendarReducer: Reducer<WorkoutCalendarState, WorkoutCalendarEvent> =
    Reducer { state, event ->
        when (event) {
            is WorkoutCalendarEvent.AddWorkoutClicked -> state
            is WorkoutCalendarEvent.DateSelected -> state.copy(
                date = event.date,
                workout = state.workouts.find { it.date == event.date }
            )

            is WorkoutCalendarEvent.WorkoutClicked -> state
            is WorkoutCalendarEvent.LoadMonth -> {
                state.copy(
                    workouts = state.workouts + dependencies.workoutRepository.getAll(
                        LocalDate(event.year, event.month, 1),
                        LocalDate(event.year, event.month, event.month.daysIn)
                    ).first(),
                    unallocatedVariationSets = GetGhostSetsForMonthUseCase(
                        event.year,
                        event.month
                    ).first()
                )
            }

            is WorkoutCalendarEvent.AddToWorkout -> state
        }
    }
