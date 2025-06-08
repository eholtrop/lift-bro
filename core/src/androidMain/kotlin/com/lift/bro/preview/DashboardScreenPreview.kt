package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.home.DashboardContent
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.JetpackComposeCoordinator
import org.jetbrains.compose.ui.tooling.preview.Preview

@Preview
@Composable
fun DashboardScreenPreview() {
    AppTheme {
        DashboardContent(
            lifts = listOf(
                Lift(
                    id = "1",
                    name = "Bench Press",
                    color = null,
                ),
                Lift(
                    id = "2",
                    name = "Bench Press",
                    color = null,
                ),
                Lift(
                    id = "3",
                    name = "Bench Press",
                    color = null,
                ),
                Lift(
                    id = "4",
                    name = "Bench Press",
                    color = null,
                )
            ),
            addLiftClicked = {},
            liftClicked = {},
            addSetClicked = {},
            setClicked = { _, _ -> },
            navCoordinator = JetpackComposeCoordinator(initialState = Destination.Dashboard)
        )
    }
}
