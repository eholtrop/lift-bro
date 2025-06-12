package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.lift.bro.domain.models.Excercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.home.DashboardContent
import com.lift.bro.presentation.home.DashboardState
import com.lift.bro.presentation.home.Tab
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import com.lift.bro.ui.today
import kotlinx.datetime.Clock
import org.jetbrains.compose.ui.tooling.preview.Preview



@Composable
fun DashboardLiftScreenPreview(isDarkMode: Boolean) {
    PreviewAppTheme(
        isDarkMode = isDarkMode
    ) {
        DashboardContent(
            dashboardState = dashboardPreviewState
                .copy(
                    excercises = listOf(
                        Excercise(
                            sets = listOf(
                                LBSet(
                                    id = "0",
                                    variationId = "0",
                                    date = Clock.System.now(),
                                    notes = ""
                                )
                            ),
                            variation = Variation(id = "0"),
                            date = today
                        ),
                        Excercise(
                            sets = listOf(
                                LBSet(
                                    id = "0",
                                    variationId = "0",
                                    date = Clock.System.now(),
                                    notes = ""
                                )
                            ),
                            variation = Variation(id = "0"),
                            date = today
                        ),
                        Excercise(
                            sets = listOf(
                                LBSet(
                                    id = "0",
                                    variationId = "0",
                                    date = Clock.System.now(),
                                    notes = ""
                                )
                            ),
                            variation = Variation(id = "0"),
                            date = today
                        ),
                        Excercise(
                            sets = listOf(
                                LBSet(
                                    id = "0",
                                    variationId = "0",
                                    date = Clock.System.now(),
                                    notes = ""
                                )
                            ),
                            variation = Variation(id = "0"),
                            date = today
                        )
                    )
                ),
            defaultTab = Tab.Lifts,
            addLiftClicked = {},
            liftClicked = {},
            addSetClicked = {},
            setClicked = { _, _ -> },
            navCoordinator = JetpackComposeCoordinator(initialState = Destination.Dashboard)
        )
    }
}

@Composable
fun DashboardCalendarScreenPreview(isDarkMode: Boolean) {
    PreviewAppTheme(
        isDarkMode = isDarkMode
    ) {
        DashboardContent(
            dashboardState = dashboardPreviewState,
            defaultTab = Tab.RecentSets,
            addLiftClicked = {},
            liftClicked = {},
            addSetClicked = {},
            setClicked = { _, _ -> },
            navCoordinator = JetpackComposeCoordinator(initialState = Destination.Dashboard)
        )
    }

}

@Preview
@Composable
fun DashboardLiftScreenPreview_Light() {
    DashboardLiftScreenPreview(false)
}

@Preview
@Composable
fun DashboardCalendarScreenPreview_Light() {
    DashboardCalendarScreenPreview(false)
}

@Preview
@Composable
fun DashboardLiftScreenPreview_Dark() {
    DashboardLiftScreenPreview(true)
}

@Preview
@Composable
fun DashboardCalendarScreenPreview_Dark() {
    DashboardCalendarScreenPreview(true)
}

val dashboardPreviewState = DashboardState(
    showEmpty = false,
    liftCards = LiftCardPreviewStates.All,
    excercises = emptyList(),
)
