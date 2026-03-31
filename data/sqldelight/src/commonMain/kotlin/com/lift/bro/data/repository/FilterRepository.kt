package com.lift.bro.data.repository

import com.lift.bro.data.core.datasource.FilterDataSource
import com.lift.bro.domain.filter.Filter
import kotlinx.coroutines.flow.Flow

class FilterRepository(
    private val dataSource: FilterDataSource,
) {
    fun listenAll(): Flow<List<Filter>> = dataSource.getAll()
    fun listen(filterId: String): Flow<Filter?> = dataSource.get(filterId)
    suspend fun save(filter: Filter) = dataSource.save(filter)
    suspend fun delete(filter: Filter) = dataSource.delete(filter)
    suspend fun deleteAll() = dataSource.deleteAll()
}
