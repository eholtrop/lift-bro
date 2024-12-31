package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Variation
import kotlinx.coroutines.flow.Flow

interface IVariationRepository {

    suspend fun deleteAll()

    fun save(id: String, liftId: String, name: String?)

    fun getAll(liftId: String): List<Variation>

    fun listenAll(liftId: String): Flow<List<Variation>>

    fun getAll(): List<Variation>

    fun listenAll(): Flow<List<Variation>>

    fun delete(id: String)

    fun get(variationId: String?): Variation?

}