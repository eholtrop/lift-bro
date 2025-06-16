package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.domain.models.Lift
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftCardData
import com.lift.bro.ui.LiftCardState
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max

@Preview
@Composable
fun EmptyLiftCardPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Empty,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PopulatedLiftCardPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Partial,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FullLiftCardPreview_Light() {
    PreviewAppTheme(
        isDarkMode = false
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Full,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun EmptyLiftCardPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Empty,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PopulatedLiftCardPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Partial,
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FullLiftCardPreview_Dark() {
    PreviewAppTheme(
        isDarkMode = true
    ) {
        LiftCard(
            state = LiftCardPreviewStates.Full,
            onClick = {}
        )
    }
}

internal object LiftCardPreviewStates {

    val Empty = LiftCardState(
        lift = Lift(
            id = "1",
            name = "Press",
            color = null,
        ),
        values = emptyList()
    )

    val Partial = LiftCardState(
        lift = Lift(
            id = "2",
            name = "Squat",
            color = null,
            maxWeight = 130.0,
        ),
        values = listOf(
            LocalDate.parse("2023-01-01") to LiftCardData(100.0, 2, 2),
            LocalDate.parse("2023-01-02") to LiftCardData(120.0, 2, 2),
        )
    )

    val Full = LiftCardState(
        lift = Lift(
            id = "3",
            name = "Deadlift",
            color = null,
            maxWeight = 135.0,
        ),
        values = listOf(
            LocalDate.parse("2023-01-01") to LiftCardData(135.0, 2, 2),
            LocalDate.parse("2023-01-02") to LiftCardData(125.0, 2, 2),
            LocalDate.parse("2023-01-01") to LiftCardData(120.0, 2, 2),
            LocalDate.parse("2023-01-02") to LiftCardData(130.0,2, 2),
            LocalDate.parse("2023-01-01") to LiftCardData(125.0,2, 2),
        )
    )

    val All = listOf(Empty, Partial, Full)
}