package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.di.dependencies
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.LiftingLog
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.plus
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.rememberInteractor

@Serializable
data class WorkoutMonthState(
    val year: Int,
    val month: Month,
    val colors: Map<LocalDate, List<ULong?>> = emptyMap(),
    val logs: Map<LocalDate, LiftingLog> = emptyMap(),
)

@Serializable
sealed class WorkoutMonthEvent

val Month.daysIn: Int
    get() = when (this) {
        Month.FEBRUARY -> 28
        Month.APRIL, Month.JUNE, Month.SEPTEMBER, Month.NOVEMBER -> 30
        Month.JANUARY, Month.MARCH, Month.MAY, Month.JULY, Month.AUGUST, Month.OCTOBER, Month.DECEMBER -> 31
        else -> 28
    }

@Composable
fun rememberWorkoutMonthInteractor(
    year: Int,
    month: Month,
) = rememberInteractor<WorkoutMonthState, WorkoutCalendarEvent>(
    initialState = WorkoutMonthState(year, month),
    source = {
        combine(
            dependencies.workoutRepository.getAll(
                startDate = LocalDate(year, month, 1),
                endDate = LocalDate(year, month, 1)
                    .plus(1, DateTimeUnit.MONTH),
            ),
            FetchVariationSetsForMonth(
                year,
                month,
            ),
            dependencies.database.logDataSource.getAll().flowToList(),
        ) { workouts, unallocatedSets, logs ->
            val workoutMap = workouts.associateBy { it.date }

            WorkoutMonthState(
                year = year,
                month = month,
                colors = (1..month.daysIn).map {
                    LocalDate(year, month, it)
                }.associateWith { date ->
                    (workoutMap[date]?.exercises ?: emptyList())
                        .map { it.sections.map { it.movements.firstOrNull { it.lift?.color != null }?.lift?.color } }
                        .flatten()
                },
                logs = logs.map {
                    LiftingLog(
                        id = it.id,
                        date = it.date,
                        notes = it.notes ?: "",
                        vibe = it.vibe_check?.toInt()
                    )
                }.associateBy { it.date },
            )
        }
    }
)
