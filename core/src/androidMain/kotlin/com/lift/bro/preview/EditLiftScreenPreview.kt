package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.EditLiftScreen
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun EditLiftScreenEmptyPreview(isDarkMode: Boolean) {
    PreviewAppTheme(isDarkMode = isDarkMode) {
        EditLiftScreen(
            lift = Lift(
                name = "",
                color = null,
            ),
            initialVariations = emptyList(),
            liftSaved = {},
            liftDeleted = {},
        )
    }
}

@Composable
fun EditLiftScreenPopulatedPreview(isDarkMode: Boolean) {
    PreviewAppTheme(isDarkMode = isDarkMode) {
        val lift = Lift(
            name = "Deadlift",
            color = null,
        )
        EditLiftScreen(
            lift = lift,
            initialVariations = listOf(
                Variation(
                    id = "",
                    lift = lift,
                    name = "Old Variation",
                ),
            ),
            liftSaved = {},
            liftDeleted = {},
        )
    }
}

@Preview
@Composable
fun LiftDetailsEmptyPreview_Light() {
    EditLiftScreenEmptyPreview(false)
}

@Preview
@Composable
fun LiftDetailsEmptyPreview_Dark() {
    EditLiftScreenEmptyPreview(true)
}

@Preview
@Composable
fun LiftDetailsPopulatedPreview_Light() {
    EditLiftScreenPopulatedPreview(false)
}

@Preview
@Composable
fun LiftDetailsPopulatedPreview_Dark() {
    EditLiftScreenPopulatedPreview(true)
}