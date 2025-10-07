package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.WorkoutDataSource
import com.lift.bro.data.core.datasource.WorkoutRow
import comliftbrodb.WorkoutQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class SqldelightWorkoutDataSource(
    private val workoutQueries: WorkoutQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : WorkoutDataSource {

    override fun listenAll(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutRow>> =
        workoutQueries.getAll(startDate = startDate, endDate = endDate).asFlow().mapToList(dispatcher).map { list ->
            list.map { WorkoutRow(id = it.id, date = it.date, warmup = it.warmup, finisher = it.finisher) }
        }

    override fun listenByDate(date: LocalDate): Flow<WorkoutRow?> =
        workoutQueries.getByDate(date = date).asFlow().mapToOneOrNull(dispatcher).map { it?.let { WorkoutRow(it.id, it.date, it.warmup, it.finisher) } }

    override fun listenById(id: String): Flow<WorkoutRow?> =
        workoutQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { it?.let { WorkoutRow(it.id, it.date, it.warmup, it.finisher) } }

    override suspend fun save(row: WorkoutRow) {
        workoutQueries.save(
            id = row.id,
            finisher = row.finisher,
            warmup = row.warmup,
            date = row.date,
        )
    }

    override suspend fun delete(id: String) {
        workoutQueries.delete(id)
    }
}
