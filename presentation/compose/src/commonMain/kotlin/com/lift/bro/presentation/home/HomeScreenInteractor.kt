package com.lift.bro.presentation.home

import androidx.compose.runtime.Composable
import com.lift.bro.data.core.analyzer.GetWorkoutHistoryUseCase
import com.lift.bro.di.dependencies
import com.lift.bro.di.getWorkoutHistoryUseCase
import com.lift.bro.di.goalsRepository
import com.lift.bro.di.liftRepository
import com.lift.bro.di.workoutGenerator
import com.lift.bro.domain.analytics.Analytics
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.toDisplayString
import com.lift.bro.domain.repositories.IGoalRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.domain.repositories.WorkoutGenerator
import com.lift.bro.presentation.ApplicationScope
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.navi.LocalNavCoordinator
import tv.dpal.navi.NavCoordinator

typealias HomeInteractor = Interactor<HomeState, HomeEvent>

enum class Tab {
    Dashboard,
    WorkoutCalendar,
}

@Serializable
sealed interface HomeState {
    @Serializable
    data object Loading: HomeState

    @Serializable
    data object Empty: HomeState

    @Serializable
    data class Content(
        val selectedTab: Tab,
        val dashboardV3: Boolean = false,
        val goals: List<String> = emptyList(),
        val aiWorkoutResult: String? = null,
        val isGeneratingWorkout: Boolean = false,
    ): HomeState
}

sealed class HomeEvent {
    data object DashboardClicked: HomeEvent()
    data object CalendarClicked: HomeEvent()
    data object AddSetClicked: HomeEvent()
    data object SettingsClicked: HomeEvent()
    data object AddLiftClicked: HomeEvent()
    data object GoalsClicked: HomeEvent()
    data object TestAIClicked: HomeEvent()
    data class AIWorkoutResult(val result: String): HomeEvent()
}

@Composable
fun rememberHomeInteractor(
    initialTab: Tab = Tab.Dashboard,
    liftRepository: ILiftRepository = dependencies.liftRepository,
    goalsRepository: IGoalRepository = dependencies.goalsRepository,
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
    analytics: Analytics = dependencies.analytics,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
    getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase = dependencies.getWorkoutHistoryUseCase,
    workoutGenerator: WorkoutGenerator = dependencies.workoutGenerator,
): HomeInteractor = rememberInteractor(
    initialState = HomeState.Loading,
    source = { state ->
        combine(
            liftRepository.listenAll(),
            goalsRepository.getAll(),
        ) { lifts, goals ->
            if (lifts.isEmpty()) {
                HomeState.Empty
            } else {
                val v3 = settingsRepository.get(Setting.DashboardV3)
                val current = state as? HomeState.Content
                HomeState.Content(
                    selectedTab = if (v3) Tab.Dashboard else current?.selectedTab ?: initialTab,
                    goals = goals.map { it.name },
                    dashboardV3 = v3,
                    aiWorkoutResult = current?.aiWorkoutResult,
                    isGeneratingWorkout = current?.isGeneratingWorkout == true
                )
            }
        }
    },
    reducers = listOf(homeReducer),
    sideEffects = listOf(aiSideEffect(getWorkoutHistoryUseCase, workoutGenerator))
)

internal val homeReducer = Reducer<HomeState, HomeEvent> { state, event ->
    when (state) {
        is HomeState.Content -> {
            when (event) {
                HomeEvent.DashboardClicked -> state.copy(selectedTab = Tab.Dashboard)
                HomeEvent.CalendarClicked -> state.copy(selectedTab = Tab.WorkoutCalendar)
                HomeEvent.AddSetClicked -> state
                HomeEvent.AddLiftClicked -> state
                HomeEvent.SettingsClicked -> state
                HomeEvent.GoalsClicked -> state
                HomeEvent.TestAIClicked -> state.copy(isGeneratingWorkout = true)
                is HomeEvent.AIWorkoutResult -> state.copy(
                    isGeneratingWorkout = false,
                    aiWorkoutResult = event.result
                )
            }
        }
        else -> state
    }
}

private fun aiSideEffect(
    getWorkoutHistoryUseCase: GetWorkoutHistoryUseCase,
    workoutGenerator: WorkoutGenerator,
): SideEffect<HomeState, HomeEvent> = SideEffect { disp, state, event ->
    when {
        event is HomeEvent.TestAIClicked && state is HomeState.Content -> {
            ApplicationScope.launch {
                val history = getWorkoutHistoryUseCase()
                val preferences = WorkoutPreferences()
                workoutGenerator.generateWorkout(history, preferences)
                    .onSuccess { template ->
                        disp(HomeEvent.AIWorkoutResult(template.toDisplayString()))
                    }
                    .onFailure { error ->
                        disp(HomeEvent.AIWorkoutResult("Failed: ${error.message}"))
                    }
            }
        }
    }
}
