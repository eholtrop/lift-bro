package com.lift.bro.domain.models

import kotlinx.coroutines.flow.Flow

interface VariationRepository {

    suspend fun deleteAll()

    fun save(id: String, liftId: String, name: String?)

    fun getAll(liftId: String): List<Variation>

    fun listenAll(liftId: String): Flow<List<Variation>>

    fun getAll(): List<Variation>

    fun delete(id: String)

    fun get(variationId: String?): Variation?

}