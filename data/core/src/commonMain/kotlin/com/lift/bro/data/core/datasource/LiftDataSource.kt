package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Lift
import kotlinx.coroutines.flow.Flow

interface LiftDataSource {
    fun listenAll(): Flow<List<Lift>>
    fun getAll(): List<Lift>
    fun get(id: String?): Flow<Lift?>
    fun save(lift: Lift): Boolean
    suspend fun deleteAll()
    suspend fun delete(id: String)
}
