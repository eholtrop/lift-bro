package com.lift.bro.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.repositories.ILiftingLogRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class LiftingLogRepository(
    private val database: LBDatabase,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ILiftingLogRepository {

    override fun getByDate(date: LocalDate): Flow<LiftingLog?> =
        database.liftingLogQueries.getByDate(date)
            .asFlow()
            .mapToOneOrNull(dispatcher)
            .map { dbLog ->
                dbLog?.let {
                    LiftingLog(
                        id = it.id,
                        date = it.date,
                        notes = it.notes ?: "",
                        vibe = it.vibe_check?.toInt(),
                    )
                }
            }
            .flowOn(dispatcher)

    override fun getAll(): Flow<List<LiftingLog>> =
        database.liftingLogQueries.getAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { dbLogs ->
                dbLogs.map { dbLog ->
                    LiftingLog(
                        id = dbLog.id,
                        date = dbLog.date,
                        notes = dbLog.notes ?: "",
                        vibe = dbLog.vibe_check?.toInt(),
                    )
                }
            }
            .flowOn(dispatcher)

    override suspend fun save(log: LiftingLog) {
        database.liftingLogQueries.save(
            id = log.id,
            date = log.date,
            notes = log.notes,
            vibe_check = log.vibe?.toLong(),
        )
    }

    override suspend fun delete(id: String) {
        database.liftingLogQueries.deleteById(id)
    }

    override suspend fun deleteAll() {
        database.liftingLogQueries.deleteAll()
    }
}
