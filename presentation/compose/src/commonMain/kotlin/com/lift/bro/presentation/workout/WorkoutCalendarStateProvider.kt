package com.lift.bro.presentation.workout

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class WorkoutCalendarStateProvider: PreviewParameterProvider<WorkoutCalendarState> {
    override val values: Sequence<WorkoutCalendarState>
        get() = sequenceOf(
            // 1. Minimal - no workout, no log, no potential exercises
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = null,
                log = null,
                potentialExercises = emptyList(),
            ),
            // 2. With log notes only
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = null,
                log = LiftingLog(
                    id = "log1",
                    date = LocalDate(2024, 1, 15),
                    notes = "Felt strong today! Great session with PR on squat.",
                    vibe = 8,
                ),
                potentialExercises = emptyList(),
            ),
            // 3. Empty workout object (no exercises, warmup, or finisher)
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = Workout(
                    id = "workout-empty",
                    date = LocalDate(2024, 1, 15),
                ),
                log = null,
                potentialExercises = emptyList(),
            ),
            // 4. Populated workout with exercises, warmup, and finisher
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = Workout(
                    id = "workout-full",
                    date = LocalDate(2024, 1, 15),
                    warmup = "5 min cardio, dynamic stretches, band pull-aparts",
                    exercises = listOf(
                        Exercise(
                            id = "ex1",
                            workoutId = "workout-full",
                            sections = listOf(
                                Section(
                                    id = "vs1",
                                    exerciseId = "",
                                    recommendedSets = emptyList(),
                                    sets = listOf(
                                        LBSet(
                                            id = "s1",
                                            variationId = "back-squat",
                                            weight = 225.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now()
                                        ),
                                        LBSet(
                                            id = "s2",
                                            variationId = "back-squat",
                                            weight = 245.0,
                                            reps = 3,
                                            rpe = 9,
                                            date = Clock.System.now()
                                        ),
                                    )
                                )
                            )
                        ),
                        Exercise(
                            id = "ex2",
                            workoutId = "workout-full",
                            sections = listOf(
                                Section(
                                    id = "vs2",
                                    exerciseId = "",
                                    recommendedSets = emptyList(),
                                    sets = listOf(
                                        LBSet(
                                            id = "s3",
                                            variationId = "flat-bench",
                                            weight = 185.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now()
                                        ),
                                        LBSet(
                                            id = "s4",
                                            variationId = "flat-bench",
                                            weight = 205.0,
                                            reps = 3,
                                            rpe = 9,
                                            date = Clock.System.now()
                                        ),
                                    )
                                )
                            )
                        ),
                    ),
                    finisher = "Planks 3x60s, farmers carry 2x50ft",
                ),
                log = null,
                potentialExercises = emptyList(),
            ),
            // 5. Potential exercises only (no workout, no log)
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = null,
                log = null,
                potentialExercises = listOf(
                    Movement(
                        id = "deadlift",
                        lift = Category(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Conventional Deadlift",
                    ) to listOf(
                        LBSet(
                            id = "s5",
                            variationId = "deadlift",
                            weight = 315.0,
                            reps = 5,
                            rpe = 7,
                            date = Clock.System.now()
                        ),
                        LBSet(
                            id = "s6",
                            variationId = "deadlift",
                            weight = 365.0,
                            reps = 3,
                            rpe = 8,
                            date = Clock.System.now()
                        ),
                    ),
                    Movement(
                        id = "ohp",
                        lift = Category(
                            name = "Overhead Press",
                            color = 0xFFFF9800uL
                        ),
                        name = "Standing OHP",
                        favourite = true,
                    ) to listOf(
                        LBSet(
                            id = "s7",
                            variationId = "ohp",
                            weight = 135.0,
                            reps = 5,
                            rpe = 8,
                            date = Clock.System.now()
                        ),
                    ),
                ),
            ),
            // 6. Full: populated workout + log + potential exercises
            WorkoutCalendarState(
                selectedDate = LocalDate(2024, 1, 15),
                selectedWorkout = Workout(
                    id = "workout-full-2",
                    date = LocalDate(2024, 1, 15),
                    warmup = "5 min cardio, dynamic stretches, band pull-aparts",
                    exercises = listOf(
                        Exercise(
                            id = "ex3",
                            workoutId = "workout-full-2",
                            sections = listOf(
                                Section(
                                    id = "vs3",
                                    recommendedSets = emptyList(),
                                    exerciseId = "",
                                    sets = listOf(
                                        LBSet(
                                            id = "s8",
                                            variationId = "back-squat-2",
                                            weight = 225.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now()
                                        ),
                                        LBSet(
                                            id = "s9",
                                            variationId = "back-squat-2",
                                            weight = 245.0,
                                            reps = 3,
                                            rpe = 9,
                                            date = Clock.System.now()
                                        ),
                                    )
                                )
                            )
                        ),
                        Exercise(
                            id = "ex4",
                            workoutId = "workout-full-2",
                            sections = listOf(
                                Section(
                                    recommendedSets = emptyList(),
                                    id = "vs4",
                                    exerciseId = "",
                                    sets = listOf(
                                        LBSet(
                                            id = "s10",
                                            variationId = "flat-bench-2",
                                            weight = 185.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now()
                                        ),
                                    )
                                )
                            )
                        ),
                    ),
                    finisher = "Planks 3x60s",
                ),
                log = LiftingLog(
                    id = "log2",
                    date = LocalDate(2024, 1, 15),
                    notes = "Solid session. Squat felt heavy but moved well.",
                    vibe = 7,
                ),
                potentialExercises = listOf(
                    Movement(
                        id = "deadlift-2",
                        lift = Category(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Conventional Deadlift",
                    ) to listOf(
                        LBSet(
                            id = "s11",
                            variationId = "deadlift-2",
                            weight = 315.0,
                            reps = 5,
                            rpe = 7,
                            date = Clock.System.now()
                        ),
                    ),
                ),
            ),
        )
}
