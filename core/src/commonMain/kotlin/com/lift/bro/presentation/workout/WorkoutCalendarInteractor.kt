package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.today
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.serialization.Serializable

@Serializable
data class WorkoutCalendarState(
    val workouts: List<Workout> = emptyList(),
    val date: LocalDate,
    val workout: Workout?,
    val logs: List<LiftingLog> = emptyList(),
)

@Composable
fun navigationSideEffects(
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

fun workoutCalendarSourceData(
    workoutRepository: WorkoutRepository = WorkoutRepository(database = dependencies.database),
    logQueries: LiftingLogQueries = dependencies.database.logDataSource,
) = combine(
    workoutRepository.getAll(
        LocalDate(today.year, today.month, 1),
        LocalDate(today.year, today.month, today.month.daysIn)
    ),
    logQueries.getAll().asFlow().mapToList(Dispatchers.IO)
) { workouts, logs ->
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
        workout = workouts.find { it.date == today }
    )
}

@Composable
fun rememberWorkoutCalendarInteractor(
    sideEffects: List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> = navigationSideEffects(),
) = rememberInteractor(
    initialState = WorkoutCalendarState(
        date = today,
        workout = null
    ),
    stateResolver = { initial, source ->
        source.copy(
            date = if (initial.date != source.date) initial.date else source.date,
            workout = if (initial.date != source.date) initial.workout else source.workout,
        )
    },
    source = workoutCalendarSourceData(),
    reducers = listOf(WorkoutCalendarReducer),
    sideEffects = sideEffects,
)

sealed interface WorkoutCalendarEvent {
    data class AddWorkoutClicked(val onDate: LocalDate): WorkoutCalendarEvent
    data class WorkoutClicked(val workout: Workout): WorkoutCalendarEvent
    data class DateSelected(val date: LocalDate): WorkoutCalendarEvent

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
                    ).first()
                )
            }
        }
    }
