package com.lift.bro.domain.filter

import kotlinx.coroutines.flow.Flow

interface FilterRepository {
    fun listenAll(): Flow<List<Filter>>

    fun listen(filterId: String): Flow<Filter?>

    fun save(filter: Filter)

    fun delete(filter: Filter)

    fun deleteAll()
}
