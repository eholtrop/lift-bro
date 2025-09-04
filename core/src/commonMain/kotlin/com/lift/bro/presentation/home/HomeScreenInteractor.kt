package com.lift.bro.presentation.home

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.navigation.Destination.*
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlin.collections.listOf

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
    data class Content(val selectedTab: Tab): HomeState
}

sealed class HomeEvent {
    data object DashboardClicked: HomeEvent()
    data object CalendarClicked: HomeEvent()
    data object AddSetClicked: HomeEvent()

    data object SettingsClicked: HomeEvent()

    data object AddLiftClicked: HomeEvent()
}

@Composable
fun rememberHomeInteractor(
    initialTab: Tab = Tab.Dashboard,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
): Interactor<HomeState, HomeEvent> = rememberInteractor(
//    initialTab,
    initialState = HomeState.Loading,
    source = { state ->
        dependencies.database.liftDataSource.listenAll().map {
            if (it.isEmpty()) HomeState.Empty else HomeState.Content(
                (state as? HomeState.Content)?.selectedTab ?: initialTab
            )
        }
    },
    reducers = listOf(
        Reducer { state, event ->
            when (state) {
                is HomeState.Content -> {
                    when (event) {
                        HomeEvent.DashboardClicked -> state.copy(selectedTab = Tab.Dashboard)
                        HomeEvent.CalendarClicked -> state.copy(selectedTab = Tab.WorkoutCalendar)
                        HomeEvent.AddSetClicked -> state
                        HomeEvent.AddLiftClicked -> state
                        HomeEvent.SettingsClicked -> state
                    }
                }

                else -> state
            }
        }
    ),
    sideEffects = listOf { state, event ->
        when (event) {
            HomeEvent.DashboardClicked -> {}
            HomeEvent.CalendarClicked -> {}
            HomeEvent.AddSetClicked -> navCoordinator.present(CreateSet())
            is HomeEvent.AddLiftClicked -> navCoordinator.present(EditLift(liftId = null))
            HomeEvent.SettingsClicked -> navCoordinator.present(Settings)
        }
    }
)