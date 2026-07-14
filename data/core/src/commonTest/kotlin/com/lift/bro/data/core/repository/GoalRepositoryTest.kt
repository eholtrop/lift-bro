package com.lift.bro.data.core.repository

import com.lift.bro.data.core.testdoubles.fakeGoalDataSource
import com.lift.bro.domain.models.Goal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GoalRepositoryTest {

    @Test
    fun `get delegates to data source`() = runTest {
        val goal = Goal(id = "g1", name = "Bench 315")
        val dataSource = fakeGoalDataSource(goals = listOf(goal))
        val repository = GoalRepository(dataSource)

        val result = repository.get("g1").first()

        assertEquals(goal, result)
        assertEquals("g1", dataSource.lastId)
    }

    @Test
    fun `getAll delegates to data source`() = runTest {
        val goals = listOf(
            Goal(id = "g1", name = "Bench 315"),
            Goal(id = "g2", name = "Squat 405")
        )
        val dataSource = fakeGoalDataSource(goals = goals)
        val repository = GoalRepository(dataSource)

        val result = repository.getAll().first()

        assertEquals(goals, result)
    }

    @Test
    fun `save delegates to data source`() = runTest {
        val goal = Goal(id = "g1", name = "Bench 315")
        val dataSource = fakeGoalDataSource()
        val repository = GoalRepository(dataSource)

        repository.save(goal)

        assertEquals(goal, dataSource.savedGoal)
    }

    @Test
    fun `delete delegates to data source`() = runTest {
        val goal = Goal(id = "g1", name = "Bench 315")
        val dataSource = fakeGoalDataSource()
        val repository = GoalRepository(dataSource)

        repository.delete(goal)

        assertEquals(goal, dataSource.deletedGoal)
    }
}
