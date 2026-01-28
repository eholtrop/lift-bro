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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.Workout
import com.lift.bro.ext.ktx.datetime.toString
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

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
                exercise.variationSets.forEachIndexed { index, (_, variation, sets) ->
                    VariationSet(
                        index = if (exercise.variationSets.size > 1) index else null,
                        variation = variation,
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
                        variationSets = listOf(
                            VariationSets(
                                id = "vs1",
                                variation = com.lift.bro.domain.models.Variation(
                                    lift = Lift(
                                        id = "lift1",
                                        name = "Squat",
                                        color = Color.Red.value
                                    ),
                                    name = "Deadlift",
                                    notes = null,
                                    favourite = true,
                                ),
                                sets = listOf(
                                    LBSet(
                                        id = "set1",
                                        variationId = "vs1",
                                    )
                                )
                            ),
                            VariationSets(
                                id = "vs1",
                                variation = com.lift.bro.domain.models.Variation(
                                    lift = Lift(
                                        id = "lift1",
                                        name = "Squat",
                                        color = Color.Blue.value
                                    ),
                                    name = "Deadlift",
                                    notes = null,
                                    favourite = true,
                                ),
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
                        variationSets = emptyList()
                    )
                )
            )
        )
}
