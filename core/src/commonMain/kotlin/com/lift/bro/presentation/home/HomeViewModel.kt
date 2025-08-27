package com.lift.bro.presentation.home

import com.lift.bro.data.LiftDataSource
import com.lift.bro.di.dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

enum class Tab {
    Dashboard,
    WorkoutCalendar,
}

@Serializable
sealed interface HomeState {
    @Serializable
    data object Loading: HomeState

    @Serializable
    data object Empty : HomeState

    @Serializable
    data class Content(val selectedTab: Tab) : HomeState
}

sealed class HomeEvents {
    data object DashboardClicked : HomeEvents()
    data object CalendarClicked : HomeEvents()
    data object AddSetClicked : HomeEvents()
}

class HomeViewModel(
    initialState: HomeState? = null,
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    scope: CoroutineScope = GlobalScope
) {

    val events: Channel<HomeEvents> = Channel()

    fun handleEvent(event: HomeEvents) {
        events.trySend(event)
    }

    val state: StateFlow<HomeState> = liftRepository.listenAll()
        .flatMapLatest { lifts ->
            events.receiveAsFlow().scan(
                initial = if (lifts.isEmpty()) HomeState.Empty else HomeState.Content(Tab.Dashboard)
            ) { state, event ->
                when (state) {
                    is HomeState.Content -> {
                        when (event) {
                            HomeEvents.DashboardClicked -> state.copy(selectedTab = Tab.Dashboard)
                            HomeEvents.CalendarClicked -> state.copy(selectedTab = Tab.WorkoutCalendar)
                            HomeEvents.AddSetClicked -> state
                        }
                    }
                    else -> state
                }
            }
        }
        .stateIn(scope, SharingStarted.Eagerly, initialState ?: HomeState.Loading)
}