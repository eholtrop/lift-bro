package com.lift.bro.presentation.home

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.di.goalsRepository
import com.lift.bro.di.liftRepository
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditLift
import com.lift.bro.ui.navigation.Destination.Settings
import kotlinx.coroutines.flow.combine
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
        val goals: List<String> = emptyList(),
    ): HomeState
}

sealed class HomeEvent {
    data object DashboardClicked: HomeEvent()
    data object CalendarClicked: HomeEvent()
    data object AddSetClicked: HomeEvent()

    data object SettingsClicked: HomeEvent()

    data object AddLiftClicked: HomeEvent()

    data object GoalsClicked: HomeEvent()
}

@Composable
fun rememberHomeInteractor(
    initialTab: Tab = Tab.Dashboard,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): HomeInteractor = rememberInteractor(
    initialState = HomeState.Loading,
    source = { state ->
        combine(
            dependencies.liftRepository.listenAll(),
            dependencies.goalsRepository.getAll(),
        ) { lifts, goals ->
            if (lifts.isEmpty()) {
                HomeState.Empty
            } else {
                HomeState.Content(
                    selectedTab = (state as? HomeState.Content)?.selectedTab ?: initialTab,
                    goals = goals.map { it.name },
                )
            }
        }
    },
    reducers = listOf(homeReducer),
    sideEffects = homeSideEffects(navCoordinator)
)

internal fun homeSideEffects(
    navCoordinator: NavCoordinator,
) = listOf<SideEffect<HomeState, HomeEvent>>(
    SideEffect { _, _, event ->
        when (event) {
            HomeEvent.DashboardClicked -> {}
            HomeEvent.CalendarClicked -> {}
            HomeEvent.AddSetClicked -> navCoordinator.present(CreateSet())
            is HomeEvent.AddLiftClicked -> navCoordinator.present(EditLift(liftId = null))
            HomeEvent.SettingsClicked -> navCoordinator.present(Settings)
            HomeEvent.GoalsClicked -> navCoordinator.present(Destination.Goals)
        }
    }
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
            }
        }

        else -> state
    }
}
