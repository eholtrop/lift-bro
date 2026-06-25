package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.di.dependencies
import com.lift.bro.di.exerciseRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.presentation.LiftBroNavCoordinator
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.ui.calendar.today
import com.lift.bro.ui.navigation.Destination
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.ktx.datetime.toLocalDate

@Serializable
data class WorkoutCalendarState(
    val selectedDate: LocalDate = today,
    val selectedWorkout: Workout? = null,
    val potentialExercises: List<Pair<Movement, List<LBSet>>> = emptyList(),
    val log: LiftingLog? = null,
)

@Composable
fun rememberWorkoutCalendarInteractor(
    initialDate: LocalDate = today,
) = rememberInteractor(
    initialState = WorkoutCalendarState(
        selectedDate = initialDate,
    ),
    source = { source -> workoutCalendarSourceData(selectedDate = source.selectedDate) },
    reducers = listOf(WorkoutCalendarReducer),
    sideEffects = navigationSideEffects() + dataSideEffects(),
)

@Composable
private fun navigationSideEffects(
    navCoordinator: LiftBroNavCoordinator = LocalNavCoordinator.current,
): List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> {
    return listOf(
        SideEffect { _, _, event ->
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

private fun dataSideEffects(): List<SideEffect<WorkoutCalendarState, WorkoutCalendarEvent>> {
    return listOf(
        SideEffect { _, state, event ->
            when (event) {
                is WorkoutCalendarEvent.AddWorkoutClicked -> {}
                is WorkoutCalendarEvent.AddToWorkout -> {
                    val workout = state.selectedWorkout
                    if (workout != null) {
                        val newId = uuid4().toString()
                        dependencies.exerciseRepository.save(
                            Exercise(
                                id = newId,
                                workoutId = workout.id,
                                sections = listOf(
                                    Section(
                                        id = uuid4().toString(),
                                        exerciseId = newId,
                                        sets = emptyList(),
                                        recommendedSets = emptyList(),
                                    )
                                )
                            )
                        )
                    } else {
                        val workoutId = state.selectedWorkout?.id ?: uuid4().toString()
                        val exerciseId = uuid4().toString()
                        dependencies.workoutRepository.save(
                            Workout(
                                id = workoutId,
                                date = event.date,
                                exercises = listOf(
                                    Exercise(
                                        id = exerciseId,
                                        workoutId = uuid4().toString(),
                                        sections = listOf(
                                            Section(
                                                id = uuid4().toString(),
                                                exerciseId = exerciseId,
                                                recommendedSets = emptyList(),
                                            )
                                        )
                                    )

                                )
                            )
                        )
                    }
                }

                is WorkoutCalendarEvent.WorkoutClicked -> {}
                is WorkoutCalendarEvent.DateSelected -> {}
            }
        }
    )
}

fun workoutCalendarSourceData(
    selectedDate: LocalDate = today,
    workoutRepository: IWorkoutRepository = dependencies.workoutRepository,
    logQueries: LiftingLogQueries = dependencies.database.logDataSource,
) = combine(
    workoutRepository.get(selectedDate),
    logQueries.getByDate(selectedDate).flowToOneOrNull(),
    FetchVariationSetsForMonth(
        selectedDate.year,
        selectedDate.month
    )
) { workout, log, unallocatedSets ->
    WorkoutCalendarState(
        selectedDate = selectedDate,
        selectedWorkout = workout,
        log = log?.let {
            LiftingLog(
                id = it.id,
                date = selectedDate,
                notes = it.notes ?: "",
                vibe = it.vibe_check?.toInt() ?: 0
            )
        },
        potentialExercises = unallocatedSets
            .filter { it.second.any { it.date.toLocalDate() == selectedDate } },
    )
}

fun FetchVariationSetsForMonth(
    year: Int,
    month: Month,
): Flow<List<Pair<Movement, List<LBSet>>>> = combine(
    dependencies.setRepository.listenAll(
        LocalDate(year = year, month = month, 1),
        LocalDate(year = year, month = month, 1)
            .plus(1, DateTimeUnit.MONTH),
    ),
    dependencies.variationRepository.listenAll().map { it.associateBy { it.id } },
) { sets, variations ->
    sets.groupBy { variations[it.variationId]!! }
        .toList()
}

fun FetchVariationSetsForRange(
    startDate: LocalDate,
    endDate: LocalDate,
): Flow<List<Pair<Movement, List<LBSet>>>> = combine(
    dependencies.setRepository.listenAll(
        startDate = startDate,
        endDate = endDate
    ),
    dependencies.variationRepository.listenAll().map { it.associateBy { it.id } },
) { sets, variations ->
    sets.groupBy { variations[it.variationId]!! }
        .toList()
}

sealed interface WorkoutCalendarEvent {
    data class AddWorkoutClicked(val onDate: LocalDate): WorkoutCalendarEvent
    data class WorkoutClicked(val workout: Workout): WorkoutCalendarEvent

    data class DateSelected(val date: LocalDate): WorkoutCalendarEvent
    data class AddToWorkout(val date: LocalDate, val variation: Movement): WorkoutCalendarEvent
}

val WorkoutCalendarReducer: Reducer<WorkoutCalendarState, WorkoutCalendarEvent> =
    Reducer { state, event ->
        when (event) {
            is WorkoutCalendarEvent.AddWorkoutClicked -> state

            is WorkoutCalendarEvent.WorkoutClicked -> state

            is WorkoutCalendarEvent.AddToWorkout -> state
            is WorkoutCalendarEvent.DateSelected -> {
                val selectedDate = event.date
                workoutCalendarSourceData(selectedDate).first()
            }
        }
    }
