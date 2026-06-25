package com.lift.bro.presentation.backup

import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lift.bro.Backup
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class BackupStateProvider : PreviewParameterProvider<BackupState> {
    override val values: Sequence<BackupState>
        get() = sequenceOf(
            BackupState(
                backup = Backup(),
                backupFinished = false,
            ),
            BackupState(
                backup = Backup(
                    lifts = listOf(
                        Category(name = "Squat", color = 0xFF2196F3uL),
                        Category(name = "Bench Press", color = 0xFF4CAF50uL),
                    ),
                    variations = listOf(
                        Movement(
                            id = "back-squat",
                            lift = Category(name = "Squat", color = 0xFF2196F3uL),
                            name = "Back Squat",
                        ),
                        Movement(
                            id = "flat-bench",
                            lift = Category(name = "Bench Press", color = 0xFF4CAF50uL),
                            name = "Flat Bench",
                        ),
                    ),
                    sets = listOf(
                        LBSet(
                            id = "set1",
                            variationId = "back-squat",
                            weight = 315.0,
                            reps = 5,
                            rpe = 8,
                            date = Clock.System.now(),
                        ),
                        LBSet(
                            id = "set2",
                            variationId = "back-squat",
                            weight = 335.0,
                            reps = 3,
                            rpe = 9,
                            date = Clock.System.now(),
                        ),
                        LBSet(
                            id = "set3",
                            variationId = "flat-bench",
                            weight = 225.0,
                            reps = 5,
                            rpe = 8,
                            date = Clock.System.now(),
                        ),
                        LBSet(
                            id = "set4",
                            variationId = "flat-bench",
                            weight = 245.0,
                            reps = 3,
                            rpe = 9,
                            date = Clock.System.now(),
                        ),
                    ),
                    workouts = listOf(
                        Workout(
                            id = "workout1",
                            date = LocalDate(2024, 1, 15),
                            warmup = "5 min cardio",
                            exercises = listOf(
                                Exercise(
                                    id = "exercise1",
                                    workoutId = "workout1",
                                    sections = listOf(
                                        Section(
                                            id = "vs1",
                                            exerciseId = "",
                                            recommendedSets = emptyList(),
                                            sets = listOf(
                                                LBSet(
                                                    id = "set1",
                                                    variationId = "back-squat",
                                                    weight = 315.0,
                                                    reps = 5,
                                                    rpe = 8,
                                                    date = Clock.System.now(),
                                                ),
                                                LBSet(
                                                    id = "set2",
                                                    variationId = "back-squat",
                                                    weight = 335.0,
                                                    reps = 3,
                                                    rpe = 9,
                                                    date = Clock.System.now(),
                                                ),
                                            ),
                                        ),
                                        Section(
                                            id = "vs2",
                                            exerciseId = "",
                                            recommendedSets = emptyList(),
                                            sets = listOf(
                                                LBSet(
                                                    id = "set3",
                                                    variationId = "flat-bench",
                                                    weight = 225.0,
                                                    reps = 5,
                                                    rpe = 8,
                                                    date = Clock.System.now(),
                                                ),
                                                LBSet(
                                                    id = "set4",
                                                    variationId = "flat-bench",
                                                    weight = 245.0,
                                                    reps = 3,
                                                    rpe = 9,
                                                    date = Clock.System.now(),
                                                ),
                                            ),
                                        ),
                                    ),
                                ),
                            ),
                            finisher = "Planks",
                        ),
                    ),
                    exercises = listOf(
                        Exercise(
                            id = "exercise1",
                            workoutId = "workout1",
                            sections = listOf(
                                Section(
                                    id = "vs1",
                                    exerciseId = "",
                                    recommendedSets = emptyList(),
                                    sets = listOf(
                                        LBSet(
                                            id = "set1",
                                            variationId = "back-squat",
                                            weight = 315.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now(),
                                        ),
                                        LBSet(
                                            id = "set2",
                                            variationId = "back-squat",
                                            weight = 335.0,
                                            reps = 3,
                                            rpe = 9,
                                            date = Clock.System.now(),
                                        ),
                                    ),
                                ),
                                Section(
                                    id = "vs2",
                                    exerciseId = "",
                                    recommendedSets = emptyList(),
                                    sets = listOf(
                                        LBSet(
                                            id = "set3",
                                            variationId = "flat-bench",
                                            weight = 225.0,
                                            reps = 5,
                                            rpe = 8,
                                            date = Clock.System.now(),
                                        ),
                                        LBSet(
                                            id = "set4",
                                            variationId = "flat-bench",
                                            weight = 245.0,
                                            reps = 3,
                                            rpe = 9,
                                            date = Clock.System.now(),
                                        ),
                                    ),
                                ),
                            ),
                        ),
                    ),
                    liftingLogs = listOf(
                        LiftingLog(
                            id = "log1",
                            date = LocalDate(2024, 1, 15),
                            notes = "Felt strong today",
                            vibe = 8,
                        ),
                        LiftingLog(
                            id = "log2",
                            date = LocalDate(2024, 1, 13),
                            notes = "Light recovery day",
                            vibe = 6,
                        ),
                    ),
                ),
                backupFinished = true,
            ),
        )
}
