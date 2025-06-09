package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.home.DashboardContent
import com.lift.bro.presentation.home.DashboardState
import com.lift.bro.presentation.home.Tab
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DashboardLiftScreenPreview() {
    AppTheme {
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
fun DashboardCalendarScreenPreview() {
    AppTheme {
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
