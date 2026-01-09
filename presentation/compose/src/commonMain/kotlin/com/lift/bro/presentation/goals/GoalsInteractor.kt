package com.lift.bro.presentation.goals

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.goalsRepository
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.models.GoalId
import com.lift.bro.domain.repositories.IGoalRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.rememberInteractor
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable

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
    reducers = listOf(
        Reducer { state, event ->
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
        },
    ),
    sideEffects = listOf { state, event ->
        when (event) {
            GoalsEvents.AddGoalClicked -> state
            is GoalsEvents.DeleteGoalClicked -> goalsRepository.delete(Goal(event.goalId, name = ""))
            is GoalsEvents.GoalChanged -> goalsRepository.save(
                state.goals.first { it.id == event.goalId }
            )

            is GoalsEvents.ToggleGoalAchieved -> goalsRepository.save(
                state.goals.first { it.id == event.goalId }
            )
        }
    }
)
