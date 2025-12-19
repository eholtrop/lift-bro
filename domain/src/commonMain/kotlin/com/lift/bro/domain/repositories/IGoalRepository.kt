package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Goal
import kotlinx.coroutines.flow.Flow

interface IGoalRepository {

    fun get(id: String): Flow<Goal?>

    fun getAll(): Flow<List<Goal>>

    suspend fun save(goal: Goal)

    suspend fun delete(goal: Goal)
}
