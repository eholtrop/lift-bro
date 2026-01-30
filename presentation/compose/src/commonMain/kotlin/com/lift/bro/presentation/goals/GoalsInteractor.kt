package com.lift.bro.presentation.goals

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.goalsRepository
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.models.GoalId
import com.lift.bro.domain.repositories.IGoalRepository
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor

typealias GoalsInteractor = Interactor<GoalsState, GoalsEvents>

@Serializable
data class GoalsState(
    val goals: List<Goal> = emptyList(),
)

sealed interface GoalsEvents {
    data object AddGoalClicked: GoalsEvents
    data class DeleteGoalClicked(val goalId: GoalId): GoalsEvents
    data class GoalChanged(val goalId: GoalId, val name: String): GoalsEvents
    data class ToggleGoalAchieved(val goalId: GoalId): GoalsEvents
}

val GoalsReducer = Reducer<GoalsState, GoalsEvents> { state, event ->
    when (event) {
        GoalsEvents.AddGoalClicked -> state.copy(goals = listOf(Goal(name = "")) + state.goals)
        is GoalsEvents.DeleteGoalClicked -> state.copy(goals = state.goals.filter { it.id != event.goalId })
        is GoalsEvents.GoalChanged -> state.copy(
            goals = state.goals.map {
                if (it.id == event.goalId) {
                    it.copy(name = event.name)
                } else {
                    it
                }
//                TestAlias
            }
        )
        is GoalsEvents.ToggleGoalAchieved -> state.copy(
            goals = state.goals.map {
                if (it.id == event.goalId) {
                    it.copy(achieved = !it.achieved)
                } else {
                    it
                }
            }
        )
    }
}

fun goalsSideEffects(
    goalsRepository: IGoalRepository = dependencies.goalsRepository
): SideEffect<GoalsState, GoalsEvents> = SideEffect { _, state, event ->
    when (event) {
        GoalsEvents.AddGoalClicked -> Unit
        is GoalsEvents.DeleteGoalClicked -> goalsRepository.delete(Goal(event.goalId, name = ""))
        is GoalsEvents.GoalChanged -> goalsRepository.save(
            state.goals.first { it.id == event.goalId }
        )
        is GoalsEvents.ToggleGoalAchieved -> goalsRepository.save(
            state.goals.first { it.id == event.goalId }
        )
    }
}

@Composable
fun rememberGoalsInteractor(
    goalsRepository: IGoalRepository = dependencies.goalsRepository,
): GoalsInteractor = rememberInteractor(
    initialState = GoalsState(),
    source = { state ->
        goalsRepository.getAll()
            .map {
                GoalsState(
                    goals = it
                )
            }
    },
    reducers = listOf(GoalsReducer),
    sideEffects = listOf(goalsSideEffects(goalsRepository))
)
