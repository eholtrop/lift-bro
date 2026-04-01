package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.LiftingLog
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ILiftingLogRepository {
    fun getByDate(date: LocalDate): Flow<LiftingLog?>

    fun getAll(): Flow<List<LiftingLog>>

    suspend fun save(log: LiftingLog)

    suspend fun delete(id: String)

    suspend fun deleteAll()
}
