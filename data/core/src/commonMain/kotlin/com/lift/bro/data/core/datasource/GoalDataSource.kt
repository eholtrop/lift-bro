package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Goal
import kotlinx.coroutines.flow.Flow

interface GoalDataSource {

    fun get(id: String): Flow<Goal?>

    fun getAll(
        limit: Long = Long.MAX_VALUE,
    ): Flow<List<Goal>>

    suspend fun save(goal: Goal)

    suspend fun delete(goal: Goal)

}
