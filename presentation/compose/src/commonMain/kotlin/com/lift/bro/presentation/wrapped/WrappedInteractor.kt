package com.lift.bro.presentation.wrapped

import androidx.compose.runtime.Composable
import com.lift.bro.data.sqldelight.datasource.toLocalDate
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.ui.today
import com.lift.bro.utils.fullName
import kotlinx.coroutines.flow.combine
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
fun rememberWrappedInteractor(): Interactor<WrappedState, WrappedEvents> {
    return rememberInteractor(
        initialState = WrappedState(),
        source = {
            flow {
                emit(
                    WrappedState(
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
