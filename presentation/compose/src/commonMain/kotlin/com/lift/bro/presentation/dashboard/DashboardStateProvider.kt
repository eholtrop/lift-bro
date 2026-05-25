package com.lift.bro.presentation.dashboard

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lift.bro.domain.models.Category
import com.lift.bro.ui.card.lift.LiftCardData
import com.lift.bro.ui.card.lift.LiftCardState
import kotlinx.datetime.LocalDate

class DashboardStateProvider : PreviewParameterProvider<DashboardState> {
    override val values: Sequence<DashboardState>
        get() = sequenceOf(
            Loading,
            Loaded(
                items = listOf(
                    DashboardListItem.Banner,
                    DashboardListItem.LiftHeader(v3 = true),
//                    DashboardListItem.WorkoutCalendar,
                ),
                sortingSettings = SortingSettings(),
            ),
            Loaded(
                items = listOf(
                    DashboardListItem.Banner,
                    DashboardListItem.LiftHeader(v3 = true),
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = Category(
                                name = "Squat",
                                color = 0xFF2196F3uL,
                            ),
                            values = listOf(
                                LocalDate(2024, 1, 15) to LiftCardData(
                                    weight = 315.0,
                                    reps = 5,
                                    rpe = 8,
                                ),
                                LocalDate(2024, 1, 12) to LiftCardData(
                                    weight = 295.0,
                                    reps = 5,
                                    rpe = 7,
                                ),
                                LocalDate(2024, 1, 8) to LiftCardData(
                                    weight = 275.0,
                                    reps = 5,
                                    rpe = 7,
                                ),
                            ),
                            maxWeight = 365.0,
                            maxReps = 8.0,
                            favourite = true,
                        ),
                    ),
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = Category(
                                name = "Bench Press",
                                color = 0xFF4CAF50uL,
                            ),
                            values = listOf(
                                LocalDate(2024, 1, 15) to LiftCardData(
                                    weight = 225.0,
                                    reps = 5,
                                    rpe = 8,
                                ),
                                LocalDate(2024, 1, 12) to LiftCardData(
                                    weight = 205.0,
                                    reps = 5,
                                    rpe = 7,
                                ),
                                LocalDate(2024, 1, 8) to LiftCardData(
                                    weight = 185.0,
                                    reps = 5,
                                    rpe = 7,
                                ),
                            ),
                            maxWeight = 255.0,
                            maxReps = 8.0,
                        ),
                    ),
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = Category(
                                name = "Deadlift",
                                color = 0xFFFF5722uL,
                            ),
                            values = listOf(
                                LocalDate(2024, 1, 15) to LiftCardData(
                                    weight = 405.0,
                                    reps = 3,
                                    rpe = 9,
                                ),
                                LocalDate(2024, 1, 8) to LiftCardData(
                                    weight = 385.0,
                                    reps = 3,
                                    rpe = 8,
                                ),
                            ),
                            maxWeight = 455.0,
                            maxReps = 5.0,
                        ),
                    ),
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = Category(
                                name = "Overhead Press",
                                color = 0xFFFF9800uL,
                            ),
                            values = listOf(
                                LocalDate(2024, 1, 15) to LiftCardData(
                                    weight = 135.0,
                                    reps = 5,
                                    rpe = 8,
                                ),
                                LocalDate(2024, 1, 10) to LiftCardData(
                                    weight = 125.0,
                                    reps = 5,
                                    rpe = 7,
                                ),
                            ),
                            maxWeight = 155.0,
                            maxReps = 6.0,
                        ),
                    ),
//                    DashboardListItem.WorkoutCalendar,
                ),
                sortingSettings = SortingSettings(),
            ),
            Loaded(
                items = listOf(
                    DashboardListItem.Banner,
                    DashboardListItem.LiftHeader(v3 = false),
                    DashboardListItem.LiftCard.Loaded(
                        LiftCardState(
                            lift = Category(
                                name = "Squat",
                                color = 0xFF2196F3uL,
                            ),
                            values = listOf(
                                LocalDate(2024, 1, 15) to LiftCardData(
                                    weight = 315.0,
                                    reps = 5,
                                    rpe = 8,
                                ),
                            ),
                            maxWeight = 365.0,
                            maxReps = 8.0,
                            favourite = true,
                        ),
                    ),
                ),
                sortingSettings = SortingSettings(
                    option = SortingOption.Latest,
                    favouritesAtTop = true,
                ),
            ),
        )
}
