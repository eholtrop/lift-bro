package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.lift.bro.data.repository.WorkoutRepository
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.dashboard.DashboardEvent
import com.lift.bro.presentation.dashboard.DashboardState
import com.lift.bro.presentation.dashboard.DashboardViewModel
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.today
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WorkoutCalendarState(
    val workouts: List<Workout> = emptyList(),
    val selectedWorkout: Workout? = null,
    val logs: List<LiftingLog> = emptyList()
)


@Composable
fun rememberWorkoutCalendarViewModel(): WorkoutCalendarViewModel {
    return rememberSaveable(
        saver = object : Saver<WorkoutCalendarViewModel, String> {
            override fun SaverScope.save(value: WorkoutCalendarViewModel): String? {
                return Json.encodeToString(value.state.value)
            }

            override fun restore(value: String): WorkoutCalendarViewModel? {
                return WorkoutCalendarViewModel(Json.decodeFromString(value))
            }
        },
        init = {
            WorkoutCalendarViewModel()
        }
    )
}

sealed interface WorkoutCalendarEvent {
    object AddWorkoutClicked : WorkoutCalendarEvent
    data class WorkoutClicked(val workout: Workout) : WorkoutCalendarEvent
    data class DateSelected(val date: LocalDate) : WorkoutCalendarEvent
}

class WorkoutCalendarViewModel(
    initialState: WorkoutCalendarState = WorkoutCalendarState(),
    workoutRepository: WorkoutRepository = WorkoutRepository(database = dependencies.database),
    logQueries: LiftingLogQueries = dependencies.database.logDataSource,
) {
    private val events: Channel<WorkoutCalendarEvent> = Channel()

    fun handleEvent(event: WorkoutCalendarEvent) {
        events.trySend(event)
    }

    val state: StateFlow<WorkoutCalendarState> = combine(
        workoutRepository.getAll(),
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
            selectedWorkout = workouts.find { it.date == today }
        )
    }.flatMapLatest { state ->
        events.receiveAsFlow().scan(state) { state, event ->
            when (event) {
                is WorkoutCalendarEvent.AddWorkoutClicked -> state
                is WorkoutCalendarEvent.DateSelected -> state.copy(selectedWorkout = state.workouts.find { it.date == event.date })
                is WorkoutCalendarEvent.WorkoutClicked -> state
            }
        }
    }
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialState
        )
}