package com.lift.bro.presentation.goals

import com.lift.bro.domain.models.Goal
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class GoalsReducerTest {

    @Test
    fun `Given state When AddGoalClicked Then adds empty goal to start of list`() = runTest {
        // Given
        val existingGoal = Goal(id = "1", name = "Existing Goal")
        val state = GoalsState(goals = listOf(existingGoal))
        val event = GoalsEvents.AddGoalClicked

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(2, result.goals.size)
        assertEquals("", result.goals.first().name)
        assertEquals("Existing Goal", result.goals[1].name)
    }

    @Test
    fun `Given empty state When AddGoalClicked Then adds empty goal`() = runTest {
        // Given
        val state = GoalsState(goals = emptyList())
        val event = GoalsEvents.AddGoalClicked

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(1, result.goals.size)
        assertEquals("", result.goals.first().name)
        assertFalse(result.goals.first().achieved)
    }

    @Test
    fun `Given state with goal When DeleteGoalClicked Then removes goal with matching id`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Goal 1")
        val goal2 = Goal(id = "2", name = "Goal 2")
        val goal3 = Goal(id = "3", name = "Goal 3")
        val state = GoalsState(goals = listOf(goal1, goal2, goal3))
        val event = GoalsEvents.DeleteGoalClicked("2")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(2, result.goals.size)
        assertEquals("Goal 1", result.goals[0].name)
        assertEquals("Goal 3", result.goals[1].name)
    }

    @Test
    fun `Given state When DeleteGoalClicked with non-existent id Then returns state unchanged`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Goal 1")
        val state = GoalsState(goals = listOf(goal1))
        val event = GoalsEvents.DeleteGoalClicked("non-existent")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(1, result.goals.size)
        assertEquals(state.goals, result.goals)
    }

    @Test
    fun `Given state with goal When GoalChanged Then updates name of matching goal`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Old Name")
        val goal2 = Goal(id = "2", name = "Another Goal")
        val state = GoalsState(goals = listOf(goal1, goal2))
        val event = GoalsEvents.GoalChanged(goalId = "1", name = "New Name")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(2, result.goals.size)
        assertEquals("New Name", result.goals[0].name)
        assertEquals("Another Goal", result.goals[1].name)
    }

    @Test
    fun `Given state When GoalChanged with non-existent id Then returns state unchanged`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Goal 1")
        val state = GoalsState(goals = listOf(goal1))
        val event = GoalsEvents.GoalChanged(goalId = "non-existent", name = "New Name")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(state.goals, result.goals)
        assertEquals("Goal 1", result.goals[0].name)
    }

    @Test
    fun `Given state with unachieved goal When ToggleGoalAchieved Then sets achieved to true`() = runTest {
        // Given
        val goal = Goal(id = "1", name = "My Goal", achieved = false)
        val state = GoalsState(goals = listOf(goal))
        val event = GoalsEvents.ToggleGoalAchieved("1")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertTrue(result.goals.first().achieved)
        assertEquals("My Goal", result.goals.first().name)
    }

    @Test
    fun `Given state with achieved goal When ToggleGoalAchieved Then sets achieved to false`() = runTest {
        // Given
        val goal = Goal(id = "1", name = "My Goal", achieved = true)
        val state = GoalsState(goals = listOf(goal))
        val event = GoalsEvents.ToggleGoalAchieved("1")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertFalse(result.goals.first().achieved)
        assertEquals("My Goal", result.goals.first().name)
    }

    @Test
    fun `Given state When ToggleGoalAchieved with non-existent id Then returns state unchanged`() = runTest {
        // Given
        val goal = Goal(id = "1", name = "Goal 1", achieved = false)
        val state = GoalsState(goals = listOf(goal))
        val event = GoalsEvents.ToggleGoalAchieved("non-existent")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(state.goals, result.goals)
        assertFalse(result.goals.first().achieved)
    }

    @Test
    fun `Given state with multiple goals When ToggleGoalAchieved Then only toggles matching goal`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Goal 1", achieved = false)
        val goal2 = Goal(id = "2", name = "Goal 2", achieved = false)
        val goal3 = Goal(id = "3", name = "Goal 3", achieved = true)
        val state = GoalsState(goals = listOf(goal1, goal2, goal3))
        val event = GoalsEvents.ToggleGoalAchieved("2")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(3, result.goals.size)
        assertFalse(result.goals[0].achieved) // Goal 1 unchanged
        assertTrue(result.goals[1].achieved)  // Goal 2 toggled
        assertTrue(result.goals[2].achieved)  // Goal 3 unchanged
    }

    @Test
    fun `Given state with multiple goals When GoalChanged Then only updates matching goal`() = runTest {
        // Given
        val goal1 = Goal(id = "1", name = "Goal 1")
        val goal2 = Goal(id = "2", name = "Goal 2")
        val goal3 = Goal(id = "3", name = "Goal 3")
        val state = GoalsState(goals = listOf(goal1, goal2, goal3))
        val event = GoalsEvents.GoalChanged(goalId = "2", name = "Updated Goal 2")

        // When
        val result = GoalsReducer(state, event)

        // Then
        assertEquals(3, result.goals.size)
        assertEquals("Goal 1", result.goals[0].name)
        assertEquals("Updated Goal 2", result.goals[1].name)
        assertEquals("Goal 3", result.goals[2].name)
    }
}
