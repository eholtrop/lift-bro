package com.lift.bro.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Workout
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.LocalDate
import tv.dpal.ext.ktx.datetime.toString

@Composable
fun RecentWorkoutCard(
    modifier: Modifier = Modifier,
    workout: Workout,
    recentWorkoutClicked: (Workout) -> Unit,
) {
    Column(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.surface, shape = MaterialTheme.shapes.medium)
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                role = Role.Button,
                onClick = {
                    recentWorkoutClicked(workout)
                }
            )
            .padding(MaterialTheme.spacing.threeQuarters),
    ) {
        Space(MaterialTheme.spacing.half)

        Text(
            text = workout.date.toString("EEE, MMM d - yyyy"),
            style = MaterialTheme.typography.titleLarge
        )

        Space(MaterialTheme.spacing.half)

        Column(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.small
            )
                .clip(MaterialTheme.shapes.small),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            workout.exercises.forEach { exercise ->
                exercise.sections
                    .filter { it.movements.isNotEmpty() }
                    .forEachIndexed { index, (_, _, sets, movements) ->
                        WorkoutCalendarMovementCard(
                            index = if (exercise.sections.size > 1) index else null,
                            movement = movements
                                .first {
                                    it.id == sets.groupBy { it.variationId }
                                        .maxBy { it.value.size }.key
                                },
                            sets = sets
                        )
                    }
            }
        }
    }
}

@Preview
@Composable
fun RecentWorkoutCardPreview(
    @PreviewParameter(DarkModeProvider::class) isDark: Boolean,
) {
    PreviewAppTheme(isDark) {
        WorkoutProvider().values.forEach {
            RecentWorkoutCard(
                workout = it,
                recentWorkoutClicked = {}
            )
        }
    }
}

class WorkoutProvider: PreviewParameterProvider<Workout> {
    override val values: Sequence<Workout>
        get() = sequenceOf(
            Workout(
                id = "w1",
                date = LocalDate(2024, 1, 12),
                exercises = listOf(
                    com.lift.bro.domain.models.Exercise(
                        id = "ex1",
                        workoutId = "w1",
                        sections = listOf(
                            Section(
                                id = "vs1",
                                exerciseId = "",
                                recommendedSets = emptyList(),
                                sets = listOf(
                                    LBSet(
                                        id = "set1",
                                        variationId = "vs1",
                                    )
                                )
                            ),
                            Section(
                                id = "vs1",
                                exerciseId = "",
                                recommendedSets = emptyList(),
                                sets = listOf(
                                    LBSet(
                                        id = "set1",
                                        variationId = "vs1",
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            Workout(
                id = "w2",
                date = LocalDate(2024, 1, 10),
                exercises = listOf(
                    com.lift.bro.domain.models.Exercise(
                        id = "ex2",
                        workoutId = "w2",
                        sections = emptyList()
                    )
                )
            )
        )
}
