package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Category
import kotlinx.coroutines.flow.Flow

interface LiftDataSource {
    fun listenAll(): Flow<List<Category>>
    fun getAll(): List<Category>
    fun get(id: String?): Flow<Category?>
    fun save(lift: Category): Boolean
    suspend fun deleteAll()
    suspend fun delete(id: String)
}
