package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Movement
import kotlinx.coroutines.flow.Flow

interface IVariationRepository {

    suspend fun deleteAll()

    fun save(variation: Movement)

    fun listenAll(liftId: String?): Flow<List<Movement>>

    fun getAll(): List<Movement>

    fun listenAll(): Flow<List<Movement>>

    fun delete(id: String)

    fun listen(id: String): Flow<Movement?>

    fun get(variationId: String?): Movement?
}
