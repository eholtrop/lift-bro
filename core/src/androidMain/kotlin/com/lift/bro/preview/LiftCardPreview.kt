package com.lift.bro.preview

import androidx.compose.runtime.Composable
import com.example.compose.AppTheme
import com.lift.bro.domain.models.Lift
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftCardState
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.max

@Preview
@Composable
fun EmptyLiftCardPreview() {
    AppTheme {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    id = "1",
                    name = "Bench Press",
                    color = null,
                ),
                values = emptyList()
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun PopulatedLiftCardPreview() {
    AppTheme {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    id = "1",
                    name = "Bench Press",
                    color = null,
                    maxWeight = 130.0,
                ),
                values = listOf(
                    LocalDate.parse("2023-01-01") to 100.0,
                    LocalDate.parse("2023-01-02") to 120.0,
                )
            ),
            onClick = {}
        )
    }
}

@Preview
@Composable
fun FullLiftCardPreview() {
    AppTheme {
        LiftCard(
            state = LiftCardState(
                lift = Lift(
                    id = "1",
                    name = "Bench Press",
                    color = null,
                    maxWeight = 135.0,
                ),
                values = listOf(
                    LocalDate.parse("2023-01-01") to 135.0,
                    LocalDate.parse("2023-01-02") to 125.0,
                    LocalDate.parse("2023-01-01") to 120.0,
                    LocalDate.parse("2023-01-02") to 130.0,
                    LocalDate.parse("2023-01-01") to 125.0,
                )
            ),
            onClick = {}
        )
    }
}