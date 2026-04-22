package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Category
import kotlinx.coroutines.flow.Flow

interface ILiftRepository {
    fun listenAll(): Flow<List<Category>>
    fun getAll(): List<Category>
    fun get(id: String?): Flow<Category?>
    fun save(lift: Category): Boolean
    suspend fun deleteAll()
    suspend fun delete(id: String)
}
