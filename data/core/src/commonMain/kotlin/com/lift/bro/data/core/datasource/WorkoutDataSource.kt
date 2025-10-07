package com.lift.bro.data.core.datasource

import kotlinx.datetime.LocalDate
import kotlinx.coroutines.flow.Flow

// Lightweight row for workout header information; repository will compose full Workout with exercises.
data class WorkoutRow(
    val id: String,
    val date: LocalDate,
    val warmup: String?,
    val finisher: String?,
)

interface WorkoutDataSource {
    fun listenAll(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<WorkoutRow>>

    fun listenByDate(date: LocalDate): Flow<WorkoutRow?>

    fun listenById(id: String): Flow<WorkoutRow?>

    suspend fun save(row: WorkoutRow)

    suspend fun delete(id: String)
}
