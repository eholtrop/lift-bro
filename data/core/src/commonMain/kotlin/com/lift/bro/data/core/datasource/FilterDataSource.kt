package com.lift.bro.data.core.datasource

import com.lift.bro.domain.filter.Filter
import kotlinx.coroutines.flow.Flow

interface FilterDataSource {
    fun getAll(): Flow<List<Filter>>
    fun get(filterId: String): Flow<Filter?>
    suspend fun save(filter: Filter)
    suspend fun delete(filter: Filter)
    suspend fun deleteAll()
}
