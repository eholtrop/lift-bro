package com.lift.bro.presentation.dashboard

import kotlinx.datetime.LocalDate
import tv.dpal.flowvi.Reducer

val dashboardReducer = Reducer<DashboardState, DashboardEvent> { state, event ->
    when (event) {
        DashboardEvent.AddLiftClicked -> state
        is DashboardEvent.LiftClicked -> state
        is DashboardEvent.FavouritesAtTopToggled -> if (state is Loaded) {
            state.copy(
                sortingSettings = state.sortingSettings.copy(
                    favouritesAtTop = !state.sortingSettings.favouritesAtTop
                )
            )
        } else {
            state
        }
        is DashboardEvent.SortingOptionSelected -> if (state is Loaded) {
            state.copy(
                sortingSettings = state.sortingSettings.copy(
                    option = event.sortingOption
                )
            )
        } else {
            state
        }
        DashboardEvent.DismissAnalyticsBanner -> state
        DashboardEvent.EnableAnalytics -> state
    }.let { newState ->
        when (newState) {
            is Loaded -> newState.copy(
                items = newState.items.sortWithSettings(newState.sortingSettings)
            )

            else -> newState
        }
    }
}

fun List<DashboardListItem>.sortWithSettings(settings: SortingSettings): List<DashboardListItem> {
    return this.sortedWith(
        Comparator { a, b ->
            when {
                a is DashboardListItem.LiftCard.Loaded && b is DashboardListItem.LiftCard.Loaded -> {
                    if ((a.state.favourite || b.state.favourite) && settings.favouritesAtTop) {
                        b.state.favourite.compareTo(a.state.favourite)
                    } else {
                        when (settings.option) {
                            SortingOption.Heaviest -> b.state.maxWeight?.compareTo(a.state.maxWeight ?: 0.0) ?: 0
                            SortingOption.Reps -> b.state.maxReps?.compareTo(a.state.maxReps ?: 0.0) ?: 0
                            SortingOption.Latest -> b.state.values.maxOfOrNull {
                                it.first
                            }?.compareTo(a.state.values.maxOfOrNull { it.first } ?: LocalDate.fromEpochDays(0)) ?: 0
                            SortingOption.Name -> a.state.lift.name.compareTo(b.state.lift.name)
                        }
                    }
                }

                else -> 0
            }
        }
    )
}
