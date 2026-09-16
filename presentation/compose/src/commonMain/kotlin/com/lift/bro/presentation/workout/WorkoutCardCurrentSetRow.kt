package com.lift.bro.presentation.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.set.RepWeightSelector
import com.lift.bro.presentation.set.TempoState
import com.lift.bro.ui.RpeSelector
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.ThemePreviews
import tv.dpal.compose.padding.vertical.padding

@Composable
fun WorkoutCardCurrentSetRow(
    modifier: Modifier = Modifier,
    set: WorkoutSet.Current,
    onCheckClicked: (WorkoutSet.Current) -> Unit,
    onSetSkipped: () -> Unit,
) {
    var recommendedSet by remember { mutableStateOf(set) }

    Column(
        modifier = modifier.padding(
            horizontal = MaterialTheme.spacing.half,
            top = MaterialTheme.spacing.threeQuarters,
            bottom = MaterialTheme.spacing.quarter,
        ).fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        ) {
            Row(
                verticalAlignment = Alignment.Bottom,
            ) {
                RepWeightSelector(
                    weight = recommendedSet.weight,
                    weightChanged = {
                        it?.let {
                            recommendedSet = recommendedSet.copy(weight = it)
                        }
                    },
                    reps = recommendedSet.reps,
                    repChanged = {
                        it?.let {
                            recommendedSet = recommendedSet.copy(reps = it)
                        }
                    },
                    rpe = recommendedSet.rpe,
                    rpeChanged = {
                    },
                    showRpe = false,
                    showInfo = false,
                )
            }
            with(recommendedSet.tempo) {
                TempoSelector(
                    tempo = TempoState(
                        ecc = this.down,
                        iso = this.hold,
                        con = this.up,
                    ),
                    tempoChanged = {
                        recommendedSet = recommendedSet.copy(
                            tempo = Tempo(
                                down = it.ecc ?: 3,
                                hold = it.iso ?: 1,
                                up = it.con ?: 1,
                            )
                        )
                    }
                )
            }
            RpeSelector(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = MaterialTheme.shapes.medium,
                )
                    .border(
                        1.dp,
                        MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.medium
                    ).clip(MaterialTheme.shapes.medium),
                rpe = recommendedSet.rpe,
                rpeChanged = {
                    recommendedSet = recommendedSet.copy(rpe = it)
                }
            )
        }
        Space(MaterialTheme.spacing.quarter)
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            IconButton(
                onClick = { onSetSkipped() }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Skip",
                )
            }
            IconButton(
                onClick = { onCheckClicked(recommendedSet) }
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Crushed",
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}

@ThemePreviews
@Composable
fun WorkoutCardCurrentSetRowPreview(
    @PreviewParameter(CurrentSetProvider::class) set: WorkoutSet.Current,
) {
    PreviewAppTheme {
        WorkoutCardCurrentSetRow(
            set = set,
            onCheckClicked = {},
            onSetSkipped = {},
        )
    }
}

class CurrentSetProvider: PreviewParameterProvider<WorkoutSet.Current> {
    override val values: Sequence<WorkoutSet.Current>
        get() =
            sequenceOf(
                // Standard working set
                WorkoutSet.Current(
                    recommendedSetId = "recommended1",
                    reps = 5,
                    weight = 225.0,
                    rpe = 7,
                    notes = null,
                    movement =
                    Movement(
                        id = "mov1",
                        lift =
                        Category(
                            id = "cat1",
                            name = "Bench Press",
                            color = 0xFF4CAF50uL,
                        ),
                        name = "Bench Press",
                    ),
                    tempo = Tempo(down = 3, hold = 1, up = 1),
                ),
                // Heavy single near max RPE, slow tempo
                WorkoutSet.Current(
                    recommendedSetId = "recommended2",
                    reps = 1,
                    weight = 405.0,
                    rpe = 9,
                    notes = null,
                    movement =
                    Movement(
                        id = "mov2",
                        lift =
                        Category(
                            id = "cat2",
                            name = "Deadlift",
                            color = 0xFFFF5722uL,
                        ),
                        name = "Conventional",
                    ),
                    tempo = Tempo(down = 4, hold = 2, up = 2),
                ),
                // High reps, RPE not entered yet
                WorkoutSet.Current(
                    recommendedSetId = "recommended3",
                    reps = 10,
                    weight = 135.0,
                    rpe = null,
                    notes = null,
                    movement =
                    Movement(
                        id = "mov3",
                        lift =
                        Category(
                            id = "cat3",
                            name = "Overhead Press",
                            color = 0xFF2196F3uL,
                        ),
                        name = "Standing",
                    ),
                    tempo = Tempo(down = 2, hold = 0, up = 1),
                ),
                // Fast tempo with notes attached
                WorkoutSet.Current(
                    recommendedSetId = "recommended4",
                    reps = 12,
                    weight = 95.0,
                    rpe = 6,
                    notes = "Grip gave out on rep 11.",
                    movement =
                    Movement(
                        id = "mov4",
                        lift =
                        Category(
                            id = "cat4",
                            name = "Pull Ups",
                            color = 0xFFFF9800uL,
                        ),
                        name = "Strict",
                    ),
                    tempo = Tempo(down = 1, hold = 0, up = 0),
                ),
            )
}
