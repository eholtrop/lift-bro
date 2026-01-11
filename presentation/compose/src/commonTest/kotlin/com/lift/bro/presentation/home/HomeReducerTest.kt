package com.lift.bro.presentation.home

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class HomeReducerTest {

    @Test
    fun `Given Content state When DashboardClicked Then sets selectedTab to Dashboard`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.WorkoutCalendar)
        val event = HomeEvent.DashboardClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(Tab.Dashboard, (result as HomeState.Content).selectedTab)
    }

    @Test
    fun `Given Content state When CalendarClicked Then sets selectedTab to WorkoutCalendar`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.Dashboard)
        val event = HomeEvent.CalendarClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(Tab.WorkoutCalendar, (result as HomeState.Content).selectedTab)
    }

    @Test
    fun `Given Content state When AddSetClicked Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.Dashboard, goals = listOf("Goal 1"))
        val event = HomeEvent.AddSetClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given Content state When AddLiftClicked Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.Dashboard)
        val event = HomeEvent.AddLiftClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given Content state When SettingsClicked Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.Dashboard)
        val event = HomeEvent.SettingsClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given Content state When GoalsClicked Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Content(selectedTab = Tab.Dashboard)
        val event = HomeEvent.GoalsClicked

        // When
        val result = homeReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given Content state with goals When tab switched Then preserves goals`() = runTest {
        // Given
        val goals = listOf("Goal 1", "Goal 2", "Goal 3")
        val state = HomeState.Content(selectedTab = Tab.Dashboard, goals = goals)
        val event = HomeEvent.CalendarClicked

        // When
        val result = homeReducer(state, event)

        // Then
        val contentResult = result as HomeState.Content
        assertEquals(Tab.WorkoutCalendar, contentResult.selectedTab)
        assertEquals(goals, contentResult.goals)
    }

    @Test
    fun `Given Loading state When any event Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Loading
        val events = listOf(
            HomeEvent.DashboardClicked,
            HomeEvent.CalendarClicked,
            HomeEvent.AddSetClicked,
            HomeEvent.AddLiftClicked,
            HomeEvent.SettingsClicked,
            HomeEvent.GoalsClicked
        )

        // When/Then
        events.forEach { event ->
            val result = homeReducer(state, event)
            assertEquals(HomeState.Loading, result)
        }
    }

    @Test
    fun `Given Empty state When any event Then returns state unchanged`() = runTest {
        // Given
        val state = HomeState.Empty
        val events = listOf(
            HomeEvent.DashboardClicked,
            HomeEvent.CalendarClicked,
            HomeEvent.AddSetClicked,
            HomeEvent.AddLiftClicked,
            HomeEvent.SettingsClicked,
            HomeEvent.GoalsClicked
        )

        // When/Then
        events.forEach { event ->
            val result = homeReducer(state, event)
            assertEquals(HomeState.Empty, result)
        }
    }
}
