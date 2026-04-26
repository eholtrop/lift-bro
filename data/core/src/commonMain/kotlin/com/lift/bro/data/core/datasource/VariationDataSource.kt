package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Movement
import kotlinx.coroutines.flow.Flow

interface VariationDataSource {
    suspend fun save(variation: Movement?)
    suspend fun delete(id: String)
    suspend fun deleteAll()

    fun listen(id: String): Flow<Movement?>
    fun listenAll(): Flow<List<Movement>>
    fun listenAllForLift(liftId: String?): Flow<List<Movement>>
    fun get(id: String): Movement?
    fun getAll(): List<Movement>
}
