package com.lift.bro.presentation.wrapped.goals

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.goalsRepository
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.repositories.IGoalRepository
import com.lift.bro.presentation.wrapped.LocalWrappedYear
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor

@Serializable
data class WrappedGoalsState(
    val goals: List<Goal> = emptyList(),
)

sealed interface WrappedGoalsEvent {

    data class GoalAdded(val goal: Goal): WrappedGoalsEvent
    data class GoalRemoved(val goal: Goal): WrappedGoalsEvent
    data class GoalNameChanged(val goal: Goal, val newName: String): WrappedGoalsEvent
}

@Composable
fun rememberWrappedGoalsInteractor(
    year: Int = LocalWrappedYear.current,
    goalsRepository: IGoalRepository = dependencies.goalsRepository,
): Interactor<WrappedGoalsState, WrappedGoalsEvent> = rememberInteractor(
    initialState = WrappedGoalsState(goals = listOf(Goal(name = ""))),
    source = {
        goalsRepository.getAll().map { WrappedGoalsState(goals = it) }
    },
    reducers = listOf(
        Reducer { state, event ->
            when (event) {
                is WrappedGoalsEvent.GoalAdded -> state.copy(goals = listOf(event.goal) + state.goals)
                is WrappedGoalsEvent.GoalNameChanged -> state.copy(
                    goals = state.goals.map {
                        if (it.id == event.goal.id) event.goal.copy(name = event.newName) else it
                    }
                )

                is WrappedGoalsEvent.GoalRemoved -> state.copy(goals = state.goals.filter { it.id != event.goal.id })
            }
        }
    ),
    sideEffects = listOf(
        SideEffect { _, state, event ->
            when (event) {
                is WrappedGoalsEvent.GoalAdded -> state
                is WrappedGoalsEvent.GoalNameChanged -> goalsRepository.save(
                    state.goals.first { it.id == event.goal.id }
                )

                is WrappedGoalsEvent.GoalRemoved -> goalsRepository.delete(event.goal)
            }
        }
    )
)
