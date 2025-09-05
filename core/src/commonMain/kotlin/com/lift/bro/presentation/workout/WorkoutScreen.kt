package com.lift.bro.presentation.workout

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.ui.Card
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.AccessibilityMinimumSize
import com.lift.bro.utils.decimalFormat
import com.lift.bro.utils.toString
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.excercise_screen_duplicate_cta
import org.jetbrains.compose.resources.stringResource


@Composable
fun CreateWorkoutScreen(
    interactor: Interactor<CreateWorkoutState, CreateWorkoutEvent>,
) {
    val state by interactor.state.collectAsState()

    CreateWorkoutScreenInternal(
        state = state,
        eventHandler = { interactor(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkoutScreenInternal(
    state: CreateWorkoutState,
    eventHandler: (CreateWorkoutEvent) -> Unit = {},
) {
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Workout")
                Text(
                    state.date.toString("EEEE, MMMM d, yyyy"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            item {
                var notes by remember(state.notes) { mutableStateOf(state.notes) }

                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = notes,
                    placeholder = { Text("Notes") },
                    onValueChange = {
                        notes = it
                        eventHandler(CreateWorkoutEvent.UpdateNotes(it))
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Notes,
                            contentDescription = null
                        )
                    }
                )
            }

            item {
                Row {
                    if (state.warmup != null) {
                        var warmup by remember { mutableStateOf(state.warmup ?: "") }
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = warmup,
                            label = { Text("Warmup") },
                            placeholder = { Text("High Knees") },
                            onValueChange = {
                                warmup = it
                                eventHandler(CreateWorkoutEvent.UpdateWarmup(it))
                            },
                        )
                    } else {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                eventHandler(CreateWorkoutEvent.UpdateWarmup(""))
                            },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Add Warmup")
                        }
                    }

                    Space(MaterialTheme.spacing.half)

                    if (state.finisher != null) {
                        var finisher by remember { mutableStateOf(state.finisher ?: "") }
                        TextField(
                            modifier = Modifier.weight(1f),
                            value = finisher,
                            label = { Text("Finisher") },
                            placeholder = { Text("Burpees") },
                            onValueChange = {
                                finisher = it
                                eventHandler(CreateWorkoutEvent.UpdateFinisher(it))
                            },
                        )
                    } else {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                eventHandler(CreateWorkoutEvent.UpdateFinisher(""))
                            },
                            colors = ButtonDefaults.outlinedButtonColors()
                        ) {
                            Text("Add Finisher")
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth()
                        .defaultMinSize(minHeight = 52.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium
                        )
                        .clip(
                            shape = MaterialTheme.shapes.medium
                        )
                        .clickable(
                            onClick = {
                                showVariationDialog = true
                            },
                            role = Role.Button
                        ).padding(
                            horizontal = MaterialTheme.spacing.one,
                            vertical = MaterialTheme.spacing.half
                        ),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                    )
                    Space(MaterialTheme.spacing.half)
                    Text(
                        "Add Exercise",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            items(state.exercises) { exercise ->
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                ) {
                    Column {
                        val coordinator = LocalNavCoordinator.current
                        Column(
                            modifier = Modifier.fillMaxWidth().clickable(
                                onClick = {
                                    coordinator.present(Destination.VariationDetails(exercise.variation.id))
                                }
                            )
                                .padding(
                                    top = MaterialTheme.spacing.threeQuarters,
                                    start = MaterialTheme.spacing.one,
                                ),
                        ) {
                            Text(
                                exercise.variation.fullName,
                                style = MaterialTheme.typography.titleLarge,
                            )

                            Text(
                                exercise.variation.maxText(),
                                style = MaterialTheme.typography.bodyLarge,
                            )

                            if (LocalTwmSettings.current) {
                                Text(
                                    "Total Weight Moved: ${"${exercise.totalWeightMoved.decimalFormat()} ${LocalUnitOfMeasure.current.value}"}",
                                    style = MaterialTheme.typography.labelLarge,
                                )
                            }

                            exercise.variation.notes?.let {
                                if (it.isNotBlank()) {
                                    Text(
                                        text = it,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }


                        exercise.sets.forEachIndexed { index, set ->
                            val coordinator = LocalNavCoordinator.current

                            var showOptionsDialog by remember { mutableStateOf(false) }

                            if (showOptionsDialog) {
                                ModalBottomSheet(
                                    onDismissRequest = {
                                        showOptionsDialog = false
                                    }
                                ) {
                                    Column {
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                                                .clickable(
                                                    onClick = {
                                                        eventHandler(CreateWorkoutEvent.DeleteSet(set))
                                                        showOptionsDialog = false
                                                    },
                                                    role = Role.Button
                                                ).padding(
                                                    horizontal = MaterialTheme.spacing.one,
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete"
                                            )
                                            Space(MaterialTheme.spacing.half)
                                            Text("Delete")
                                        }
                                        Row(
                                            modifier = Modifier.fillMaxWidth()
                                                .defaultMinSize(minHeight = Dp.AccessibilityMinimumSize)
                                                .clickable(
                                                    onClick = {
                                                        eventHandler(CreateWorkoutEvent.DeleteSet(set))
                                                        showOptionsDialog = false
                                                    },
                                                    role = Role.Button
                                                ).padding(
                                                    horizontal = MaterialTheme.spacing.one,
                                                ),
                                            verticalAlignment = Alignment.CenterVertically,
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ContentCopy,
                                                contentDescription = "Copy"
                                            )
                                            Space(MaterialTheme.spacing.half)
                                            Text("Copy")
                                        }
                                    }
                                }
                            }

                            SetInfoRow(
                                modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 52.dp)
                                    .animateItem()
                                    .combinedClickable(
                                        onClick = {
                                            coordinator.present(Destination.EditSet(setId = set.id))
                                        },
                                        onLongClick = {
                                            showOptionsDialog = true
                                        },
                                        role = Role.Button
                                    )
                                    .padding(
                                        horizontal = MaterialTheme.spacing.one,
                                        vertical = MaterialTheme.spacing.half
                                    ).animateItem(),
                                set = set
                            )
                        }

                        if (exercise.sets.isEmpty()) {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    coordinator.present(
                                        Destination.CreateSet(
                                            variationId = exercise.variation.id,
                                            date = state.date.atStartOfDayIn(TimeZone.currentSystemDefault()),
                                        )
                                    )
                                }
                            ) {
                                Text("Add Set")
                            }
                        } else {
                            Button(
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                colors = ButtonDefaults.elevatedButtonColors(),
                                onClick = {
                                    eventHandler(CreateWorkoutEvent.DuplicateSet(exercise.sets.last()))
                                }
                            ) {
                                Text(stringResource(Res.string.excercise_screen_duplicate_cta))
                            }
                        }
                    }
                }
            }
        }
    }

    VariationSearchDialog(
        visible = showVariationDialog,
        textFieldPlaceholder = "Add Exercise",
        onDismissRequest = {
            showVariationDialog = false
        },
        onVariationSelected = {
            showVariationDialog = false
            eventHandler(CreateWorkoutEvent.AddExercise(it))
        }
    )
}