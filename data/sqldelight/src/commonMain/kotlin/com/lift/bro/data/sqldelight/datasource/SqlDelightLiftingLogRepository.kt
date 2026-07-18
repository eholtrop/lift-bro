package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.repositories.ILiftingLogRepository
import comliftbrodb.LiftingLogQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class SqlDelightLiftingLogRepository(
    private val liftingLogQueries: LiftingLogQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ILiftingLogRepository {

    override fun getByDate(date: LocalDate): Flow<LiftingLog?> =
        liftingLogQueries.getByDate(date)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
            .flowOn(dispatcher)

    override fun getAll(): Flow<List<LiftingLog>> =
        liftingLogQueries.getAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { list -> list.map { it.toDomain() } }
            .flowOn(dispatcher)

    override suspend fun save(log: LiftingLog) {
        liftingLogQueries.save(
            id = log.id,
            date = log.date,
            notes = log.notes,
            vibe_check = log.vibe?.toLong(),
        )
    }
}

private fun comliftbrodb.LiftingLog.toDomain(): LiftingLog = LiftingLog(
    id = this.id,
    date = this.date,
    notes = this.notes ?: "",
    vibe = this.vibe_check?.toInt(),
)
