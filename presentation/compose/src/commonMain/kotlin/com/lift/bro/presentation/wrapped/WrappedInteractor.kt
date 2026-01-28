package com.lift.bro.presentation.wrapped

import androidx.compose.runtime.Composable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.rememberInteractor
import kotlinx.coroutines.flow.flow

val heavyThings = listOf(
    HeavyThing(
        name = "Blue Whale",
        weight = 300000.0,
        icon = "\uD83D\uDC0B"
    ),
    HeavyThing(
        name = "Elephant",
        weight = 15432.0,
        icon = "\uD83D\uDC18"
    ),
    HeavyThing(
        name = "Truck",
        weight = 5000.0,
        icon = "\uD83D\uDEFB",
    )
)

@Composable
fun rememberWrappedInteractor(year: Int): Interactor<WrappedState, WrappedEvents> {
    return rememberInteractor(
        initialState = WrappedState(year),
        source = {
            flow {
                emit(
                    WrappedState(
                        year = year,
                        pages = listOf(
                            WrappedPageState.Tenure,
                            WrappedPageState.Weight,
                            WrappedPageState.Reps,
                            WrappedPageState.Consistency,
                            WrappedPageState.Progress,
                            WrappedPageState.Goals,
                            WrappedPageState.Summary,
                        )
                    )
                )
            }
        }
    )
}
