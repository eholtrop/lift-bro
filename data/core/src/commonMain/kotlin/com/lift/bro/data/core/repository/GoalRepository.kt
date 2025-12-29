package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.GoalDataSource
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.repositories.IGoalRepository
import kotlinx.coroutines.flow.Flow

class GoalRepository(
    val goalDataSource: GoalDataSource
): IGoalRepository {


    override fun get(id: String): Flow<Goal?> = goalDataSource.get(id)

    override fun getAll(): Flow<List<Goal>> = goalDataSource.getAll()


    override suspend fun save(goal: Goal) = goalDataSource.save(goal)


    override suspend fun delete(goal: Goal) = goalDataSource.delete(goal)
}
