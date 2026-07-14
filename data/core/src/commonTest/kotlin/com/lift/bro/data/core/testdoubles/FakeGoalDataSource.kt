package com.lift.bro.data.core.testdoubles

import com.lift.bro.data.core.datasource.GoalDataSource
import com.lift.bro.domain.models.Goal
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeGoalDataSource(
    private val goals: List<Goal> = emptyList()
) : GoalDataSource {
    var lastId: String? = null
        private set
    var savedGoal: Goal? = null
        private set
    var deletedGoal: Goal? = null
        private set

    override fun get(id: String): Flow<Goal?> {
        lastId = id
        return flowOf(goals.firstOrNull { it.id == id })
    }

    override fun getAll(limit: Long): Flow<List<Goal>> = flowOf(goals)

    override suspend fun save(goal: Goal) {
        savedGoal = goal
    }

    override suspend fun delete(goal: Goal) {
        deletedGoal = goal
    }
}

fun fakeGoalDataSource(
    goals: List<Goal> = emptyList()
): FakeGoalDataSource = FakeGoalDataSource(goals)
