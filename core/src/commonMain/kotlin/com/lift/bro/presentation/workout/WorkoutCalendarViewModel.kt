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
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.SideEffect
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
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
    val selectedDate: Pair<LocalDate, Workout?>,
    val logs: List<LiftingLog> = emptyList()
)

@Composable
fun navigationSideEffects(
    navCoordinator: NavCoordinator = LocalNavCoordinator.current
): List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> {
    return listOf(
        { state, event ->
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
    )
}

fun workoutCalendarSourceData(
    workoutRepository: WorkoutRepository = WorkoutRepository(database = dependencies.database),
    logQueries: LiftingLogQueries = dependencies.database.logDataSource
) = combine(
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
        selectedDate = today to workouts.find { it.date == today }
    )
}

@Composable
fun rememberWorkoutCalendarInteractor(
    sideEffects: List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> = navigationSideEffects()
) = rememberInteractor(
    initialState = WorkoutCalendarState(selectedDate = today to null),
    stateResolver = { initial, source -> source.copy(selectedDate = initial?.selectedDate ?: source.selectedDate) },
    source = workoutCalendarSourceData(),
    reducers = listOf(WorkoutCalendarReducer),
    sideEffects = sideEffects
)

@Composable
fun rememberWorkoutCalendarViewModel(
    sideEffects: List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> = navigationSideEffects()
): WorkoutCalendarViewModel {
    return rememberSaveable(
        saver = object : Saver<WorkoutCalendarViewModel, String> {
            override fun SaverScope.save(value: WorkoutCalendarViewModel): String? {
                return Json.encodeToString(value.state.value)
            }

            override fun restore(value: String): WorkoutCalendarViewModel? {
                return WorkoutCalendarViewModel(
                    initialState = Json.decodeFromString(value),
                    sideEffects = sideEffects,
                )
            }
        },
        init = {
            WorkoutCalendarViewModel(
                sideEffects = sideEffects
            )
        }
    )
}

sealed interface WorkoutCalendarEvent {
    data class AddWorkoutClicked(val onDate: LocalDate) : WorkoutCalendarEvent
    data class WorkoutClicked(val workout: Workout) : WorkoutCalendarEvent
    data class DateSelected(val date: LocalDate) : WorkoutCalendarEvent
}

val WorkoutCalendarReducer: Reducer<WorkoutCalendarState, WorkoutCalendarEvent> = Reducer { state, event ->
    when (event) {
        is WorkoutCalendarEvent.AddWorkoutClicked -> state
        is WorkoutCalendarEvent.DateSelected -> state.copy(selectedDate = event.date to state.workouts.find { it.date == event.date })
        is WorkoutCalendarEvent.WorkoutClicked -> state
    }
}

class WorkoutCalendarViewModel(
    initialState: WorkoutCalendarState? = null,
    reducer: Reducer<WorkoutCalendarState, WorkoutCalendarEvent> = WorkoutCalendarReducer,
    sideEffects: List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> = emptyList(),
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
            selectedDate = today to workouts.find { it.date == today }
        )
    }.flatMapLatest { state ->
        events.receiveAsFlow()
            .scan(
                initial = state.copy(selectedDate = initialState?.selectedDate ?: state.selectedDate),
            ) { state, event ->
                val newState = reducer(state, event)
                sideEffects.forEach {
                    it(newState, event)
                }
                newState
            }
    }
        .stateIn(
            scope = GlobalScope,
            started = SharingStarted.WhileSubscribed(),
            initialValue = initialState ?: WorkoutCalendarState(selectedDate = today to null)
        )
}