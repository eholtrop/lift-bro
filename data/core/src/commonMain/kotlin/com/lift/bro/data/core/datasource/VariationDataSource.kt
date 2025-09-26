package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Variation
import kotlinx.coroutines.flow.Flow

interface VariationDataSource {
    suspend fun save(variation: Variation)
    suspend fun delete(id: String)
    suspend fun deleteAll()

    fun listen(id: String): Flow<Variation?>
    fun listenAll(): Flow<List<Variation>>
    fun listenAllForLift(liftId: String): Flow<List<Variation>>

    fun get(id: String): Variation?
    fun getAll(): List<Variation>
}
