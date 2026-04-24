package com.lift.bro.presentation.movement

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class MovementDetailsStateProvider: PreviewParameterProvider<MovementDetailsState> {
    override val values: Sequence<MovementDetailsState>
        get() = sequenceOf(
            MovementDetailsState(
                movement = Movement(
                    id = "minimal",
                    lift = Category(
                        name = "Squat",
                        color = 0xFF2196F3uL
                    ),
                    name = "Back Squat",
                    favourite = true,
                ),
                cards = emptyList()
            ),
            MovementDetailsState(
                movement = Movement(
                    id = "empty",
                    lift = Category(
                        name = "Deadlift",
                        color = 0xFFFF5722uL
                    ),
                    name = "Conventional Deadlift",
                    favourite = true,
                    notes = "Sumo stance on heavy days",
                    bodyWeight = true,
                ),
                cards = emptyList()
            ),
            MovementDetailsState(
                movement = Movement(
                    id = "single-card",
                    lift = Category(
                        name = "Bench Press",
                        color = 0xFF4CAF50uL
                    ),
                    name = "Flat Bench",
                    favourite = true,
                    bodyWeight = false,
                ),
                cards = listOf(
                    MovementDetailsCard(
                        title = "Monday, Jan 15",
                        sets = listOf(
                            LBSet(
                                id = "set1",
                                variationId = "single-card",
                                weight = 225.0,
                                reps = 5,
                                rpe = 8,
                                date = Clock.System.now()
                            ),
                            LBSet(
                                id = "set2",
                                variationId = "single-card",
                                weight = 245.0,
                                reps = 3,
                                rpe = 9,
                                date = Clock.System.now()
                            ),
                        )
                    )
                )
            ),
            MovementDetailsState(
                movement = Movement(
                    id = "multi-card",
                    lift = Category(
                        name = "Overhead Press",
                        color = 0xFFFF9800uL
                    ),
                    name = "Standing OHP",
                    favourite = true,
                    notes = "Strict press, no leg drive",
                ),
                cards = listOf(
                    MovementDetailsCard(
                        title = "Today",
                        sets = listOf(
                            LBSet(
                                id = "set3",
                                variationId = "multi-card",
                                weight = 135.0,
                                reps = 5,
                                tempo = Tempo(down = 3, hold = 0, up = 1),
                                rpe = 7,
                                date = Clock.System.now()
                            ),
                            LBSet(
                                id = "set4",
                                variationId = "multi-card",
                                weight = 145.0,
                                reps = 3,
                                tempo = Tempo(down = 3, hold = 0, up = 1),
                                rpe = 8,
                                date = Clock.System.now()
                            ),
                        )
                    ),
                    MovementDetailsCard(
                        title = "Wednesday, Jan 10",
                        sets = listOf(
                            LBSet(
                                id = "set5",
                                variationId = "multi-card",
                                weight = 95.0,
                                reps = 8,
                                tempo = Tempo(down = 2, hold = 1, up = 2),
                                rpe = 6,
                                notes = "Warm-up",
                                date = Clock.System.now() - 5.days
                            ),
                            LBSet(
                                id = "set6",
                                variationId = "multi-card",
                                weight = 115.0,
                                reps = 6,
                                tempo = Tempo(down = 2, hold = 1, up = 2),
                                rpe = 7,
                                date = Clock.System.now() - 5.days
                            ),
                            LBSet(
                                id = "set7",
                                variationId = "multi-card",
                                weight = 135.0,
                                reps = 4,
                                tempo = Tempo(down = 3, hold = 0, up = 1),
                                rpe = 9,
                                notes = "Grinder",
                                date = Clock.System.now() - 5.days
                            ),
                        )
                    ),
                )
            ),
            MovementDetailsState(
                movement = Movement(
                    id = "body-weight",
                    lift = Category(
                        name = "Pull-ups",
                        color = 0xFF9C27B0uL
                    ),
                    name = "Weighted Pull-ups",
                    notes = "Add 25lb for working sets",
                    bodyWeight = false,
                ),
                cards = listOf(
                    MovementDetailsCard(
                        title = "Tuesday, Jan 16",
                        sets = listOf(
                            LBSet(
                                id = "set8",
                                variationId = "body-weight",
                                weight = 25.0,
                                reps = 5,
                                rpe = 8,
                                bodyWeightRep = false,
                                date = Clock.System.now()
                            ),
                            LBSet(
                                id = "set9",
                                variationId = "body-weight",
                                weight = 0.0,
                                reps = 10,
                                rpe = 6,
                                bodyWeightRep = true,
                                date = Clock.System.now()
                            ),
                        )
                    )
                )
            ),
        )
}
