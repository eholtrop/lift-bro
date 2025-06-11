package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.lift.bro.presentation.home.DashboardContent
import com.lift.bro.presentation.home.DashboardState
import com.lift.bro.presentation.home.Tab
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

@Preview
@Composable
fun DashboardLiftScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        DashboardContent(
            dashboardState = dashboardPreviewState,
            defaultTab = Tab.Lifts,
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
fun DashboardCalendarScreenPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
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
fun DashboardLiftScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        DashboardContent(
            dashboardState = dashboardPreviewState,
            defaultTab = Tab.Lifts,
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
fun DashboardCalendarScreenPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
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

val dashboardPreviewState = DashboardState(
    showEmpty = false,
    liftCards = LiftCardPreviewStates.All,
    excercises = emptyList(),
)
