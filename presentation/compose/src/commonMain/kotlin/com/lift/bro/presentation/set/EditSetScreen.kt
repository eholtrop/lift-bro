@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.presentation.set.components.EditSetVariationSelector
import com.lift.bro.ui.Fade
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.RpeSelector
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.calendar.Calendar
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import tv.dpal.compose.padding.vertical.padding
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.ktx.datetime.atStartOfDayIn
import tv.dpal.ktx.datetime.toLocalDate
import tv.dpal.navi.LocalNavCoordinator

enum class RPE(
    val rpe: Int,
    val rir: Int,
    val percentMax: Float,
    val emoji: String,
) {
    Five(5, rir = 6, percentMax = .6f, "😇"),
    Six(6, rir = 4, percentMax = .75f, "😀"),
    Seven(7, rir = 3, percentMax = .85f, "💪"),
    Eight(8, rir = 2, percentMax = .9f, "😰"),
    Nine(9, rir = 1, percentMax = .95f, "🥵"),
    Ten(10, rir = 0, percentMax = 1f, "💀")
}

@Composable
fun EditSetScreen(
    variationId: String?,
    date: Instant?,
) {
    EditSetScreen(
        interactor = rememberCreateSetInteractor(
            variationId = variationId,
            date = date
        ),
    )
}

@Composable
fun EditSetScreen(
    setId: String,
) {
    EditSetScreen(
        interactor = rememberEditSetInteractor(
            setId = setId,
        ),
    )
}

@Composable
fun EditSetScreen(
    interactor: Interactor<EditSetState?, EditSetEvent>,
) {
    val state by interactor.state.collectAsState()

    state?.let {
        EditSetScreen(
            state = it,
            onEvent = { interactor(it) }
        )
    }
}

@Composable
fun EditSetScreen(
    state: EditSetState,
    onEvent: (EditSetEvent) -> Unit,
    strings: EditSetScreenStrings = EditSetScreenStrings.default(),
) {
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            Text(
                if (state.id == null) strings.createSetTitle else strings.editSetTitle
            )
        },
        trailingContent = {
            Fade(visible = state.saveEnabled) {
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = strings.deleteContentDescription,
                    onClick = {
                        onEvent(EditSetEvent.DeleteSetClicked)
                    }
                )
            }

            Switch(
                checked = state.showV2,
                onCheckedChange = { onEvent(EditSetEvent.ToggleV2) },
                thumbContent = {
                    Text(
                        if (state.showV2) "V2" else "V1",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            )
            if (state.timerEnabled) {
                val navCoordinator = LocalNavCoordinator.current
                val tempo = state.let {
                    Tempo(
                        down = it.tempo.ecc ?: 3,
                        hold = it.tempo.iso ?: 1,
                        up = it.tempo.con ?: 1
                    )
                }
                IconButton(
                    onClick = {
                        val id = state.id ?: ""
                        if (state.saveEnabled && id.isNotBlank()) {
                            navCoordinator.present(
                                Destination.Timer.From(setId = id)
                            )
                        } else {
                            navCoordinator.present(
                                Destination.Timer.With(reps = state.reps?.toInt() ?: 1, tempo = tempo)
                            )
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = strings.timerContentDescription
                    )
                }
            }
        },
    ) { padding ->
        state.let { set ->
            if (set.showV2) {
                EditSetScreenV2(
                    modifier = Modifier.padding(padding),
                    state = set,
                    sendEvent = { onEvent(it) },
                    showVariationDialog = {
                        showVariationDialog = true
                    },
                    strings = strings,
                )
            } else {
                EditSetScreen(
                    modifier = Modifier.padding(padding),
                    state = set,
                    sendEvent = { onEvent(it) },
                    showVariationDialog = {
                        showVariationDialog = true
                    },
                    strings = strings,
                )
            }
        }
    }

    if (showVariationDialog) {
        VariationSearchDialog(
            visible = showVariationDialog,
            textFieldPlaceholder = strings.variationSelectorEmptyState,
            onDismissRequest = { showVariationDialog = false },
            onVariationSelected = {
                showVariationDialog = false
                onEvent(EditSetEvent.VariationSelected(it))
            }
        )
    }
}

@Composable
fun EditSetScreen(
    modifier: Modifier = Modifier,
    state: EditSetState,
    sendEvent: (EditSetEvent) -> Unit,
    showVariationDialog: () -> Unit,
    strings: EditSetScreenStrings = EditSetScreenStrings.default(),
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(
            horizontal = MaterialTheme.spacing.half,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {
        item {
            EditSetVariationSelector(state, showVariationDialog = showVariationDialog)
        }

        item {
            RepWeightSelector(
                modifier = Modifier.fillMaxWidth(),
                repChanged = { sendEvent(EditSetEvent.RepChanged(it)) },
                weightChanged = { sendEvent(EditSetEvent.WeightChanged(it)) },
                rpeChanged = { sendEvent(EditSetEvent.RpeChanged(it)) },
                weight = state.weight,
                reps = state.reps,
                rpe = state.rpe,
                showRpe = true
            )
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent,
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ).clip(MaterialTheme.shapes.medium),
            ) {
                TempoSelector(
                    modifier = Modifier.padding(
                        top = MaterialTheme.spacing.one,
                        bottom = MaterialTheme.spacing.quarter,
                        horizontal = MaterialTheme.spacing.quarter
                    ),
                    tempo = state.tempo,
                    tempoChanged = { sendEvent(EditSetEvent.TempoChanged(it)) }
                )
            }
        }

        item {
            Column(
                modifier = Modifier.animateContentSize()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent
                            ),
                        ),
                        shape = MaterialTheme.shapes.medium
                    ).clip(MaterialTheme.shapes.medium)

            ) {
                var showCalendar by remember { mutableStateOf(false) }

                Row {
                    Column(
                        modifier = Modifier.weight(1f)
                            .clickable(
                                onClick = { showCalendar = !showCalendar }
                            )
                            .padding(
                                vertical = MaterialTheme.spacing.half,
                                horizontal = MaterialTheme.spacing.one
                            ),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(
                            text = state.date.toString("EEEE, MMM d"),
                            color = if (showCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.date.toString("yyyy"),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f)
                            .padding(
                                vertical = MaterialTheme.spacing.half,
                                horizontal = MaterialTheme.spacing.one
                            ),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = state.date.toString("hh:mm"),
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = state.date.toString("aa").lowercase(),
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }

                AnimatedVisibility(showCalendar) {
                    var date by remember(state.date) { mutableStateOf(state.date.toLocalDate()) }

                    Calendar(
                        modifier = Modifier.fillMaxWidth()
                            .padding(
                                vertical = MaterialTheme.spacing.half,
                                horizontal = MaterialTheme.spacing.one
                            ),
                        selectedDate = date,
                        dateSelected = {
                            date = it
                            sendEvent(EditSetEvent.DateSelected(it))
                            showCalendar = false
                        },
                    )
                }
            }
        }

        item {
            Column(
                modifier = Modifier.background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            Color.Transparent,
                        )
                    ),
                    shape = MaterialTheme.shapes.medium,
                ).clip(MaterialTheme.shapes.medium),
            ) {
                var notes by remember { mutableStateOf(state.notes) }
                TextField(
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.transparentColors(),
                    value = notes,
                    singleLine = true,
                    placeholder = {
                        Text(strings.extraNotesPlaceholder)
                    },
                    label = {
                        Text(strings.extraNotesLabel)
                    },
                    onValueChange = {
                        notes = it
                        sendEvent(EditSetEvent.NotesChanged(it))
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditSetScreenV2(
    modifier: Modifier = Modifier,
    state: EditSetState,
    sendEvent: (EditSetEvent) -> Unit,
    showVariationDialog: () -> Unit,
    strings: EditSetScreenStrings = EditSetScreenStrings.default(),
) {
    SharedTransitionLayout {
        LazyColumn(
            modifier = modifier.fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.half,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            item {
                EditSetVariationSelector(state, showVariationDialog = showVariationDialog)
            }

            item {
                Box {
                    Column(
                        modifier = Modifier
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        Color.Transparent,
                                    ),
                                ),
                                shape = MaterialTheme.shapes.medium,
                            ).padding(all = MaterialTheme.spacing.half),
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                    ) {
                        RepWeightSelector(
                            modifier = Modifier.fillMaxWidth(),
                            repChanged = { sendEvent(EditSetEvent.RepChanged(it)) },
                            weightChanged = { sendEvent(EditSetEvent.WeightChanged(it)) },
                            rpeChanged = { sendEvent(EditSetEvent.RpeChanged(it)) },
                            weight = state.weight,
                            reps = state.reps,
                            rpe = state.rpe,
                            showRpe = false
                        )

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            TempoSelector(
                                tempo = state.tempo,
                                tempoChanged = { sendEvent(EditSetEvent.TempoChanged(it)) }
                            )
                        }

                        var rpe by remember {
                            mutableStateOf(
                                state.rpe
                                    ?: state.defaultRpe
                            )
                        }
                        Column {
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
                                rpe = rpe,
                                rpeChanged = {
                                    rpe = it
                                    sendEvent(EditSetEvent.RpeChanged(it))
                                }
                            )
                        }
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent,
                            )
                        ),
                        shape = MaterialTheme.shapes.medium,
                    ).clip(MaterialTheme.shapes.medium),
                ) {
                    var notes by remember { mutableStateOf(state.notes) }
                    TextField(
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.transparentColors(),
                        value = notes,
                        singleLine = true,
                        placeholder = {
                            Text(strings.extraNotesPlaceholder)
                        },
                        label = {
                            Text(strings.extraNotesLabel)
                        },
                        onValueChange = {
                            notes = it
                            sendEvent(EditSetEvent.NotesChanged(it))
                        },
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.animateContentSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    Color.Transparent
                                ),
                            ),
                            shape = MaterialTheme.shapes.medium
                        ).clip(MaterialTheme.shapes.medium)

                ) {
                    var showCalendar by remember { mutableStateOf(false) }

                    Row {
                        Column(
                            modifier = Modifier.weight(1f)
                                .clickable(
                                    onClick = { showCalendar = !showCalendar }
                                )
                                .padding(
                                    vertical = MaterialTheme.spacing.half,
                                    horizontal = MaterialTheme.spacing.one
                                ),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = state.date.toString("EEEE, MMM d"),
                                color = if (showCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.date.toString("yyyy"),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f)
                                .padding(
                                    vertical = MaterialTheme.spacing.half,
                                    horizontal = MaterialTheme.spacing.one
                                ),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text(
                                text = state.date.toString("hh:mm"),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.date.toString("aa").lowercase(),
                                style = MaterialTheme.typography.labelMedium,
                            )
                        }
                    }

                    AnimatedVisibility(showCalendar) {
                        var date by remember(state.date) { mutableStateOf(state.date.toLocalDate()) }

                        Calendar(
                            modifier = Modifier.fillMaxWidth()
                                .padding(
                                    vertical = MaterialTheme.spacing.half,
                                    horizontal = MaterialTheme.spacing.one
                                ),
                            selectedDate = date,
                            dateSelected = {
                                date = it
                                sendEvent(EditSetEvent.DateSelected(it))
                                showCalendar = false
                            },
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChipButton(
    onClick: () -> Unit,
    visible: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    AnimatedVisibility(
        visible,
        exit = fadeOut(),
    ) {
        Button(
            modifier = Modifier.height(24.dp),
            onClick = onClick,
            content = content,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.quarter),
            shape = MaterialTheme.shapes.small,
        )
    }
}

class EditSetStateProvider: PreviewParameterProvider<EditSetState> {
    override val values: Sequence<EditSetState>
        get() = sequenceOf(
            // New set - no variation selected yet
            EditSetState(
                id = null,
                variation = null,
                weight = null,
                reps = null,
                rpe = 6,
                date = LocalDate(2025, 10, 20).atStartOfDayIn(),
                showV2 = false,
            ),
            // New set with variation but no data
            EditSetState(
                id = null,
                variation = SetVariation(
                    Variation(
                        lift = Lift(
                            name = "Squat",
                            color = 0xFF2196F3uL
                        ),
                        name = "Back Squat"
                    )
                ),
                weight = null,
                reps = null,
                date = LocalDate(2025, 10, 20).atStartOfDayIn(),
                tempo = TempoState(
                    ecc = 3,
                    iso = 1,
                    con = 1
                ),
                showV2 = true,
            ),
            // Partially filled set
            EditSetState(
                id = "set1",
                variation = SetVariation(
                    Variation(
                        lift = Lift(
                            name = "Bench Press",
                            color = 0xFF4CAF50uL
                        ),
                        name = "Flat Bench"
                    )
                ),
                weight = 225.0,
                reps = 5,
                rpe = null,
                notes = "",
                tempo = TempoState(
                    ecc = 3,
                    iso = 1,
                    con = 1
                ),
                date = LocalDate(2025, 10, 20).atStartOfDayIn(),
                showV2 = true,
            ),
            // Complete set with all data
            EditSetState(
                id = "set2",
                variation = SetVariation(
                    Variation(
                        lift = Lift(
                            name = "Deadlift",
                            color = 0xFFFF5722uL
                        ),
                        name = "Conventional"
                    ),
                    variationMaxPercentage = EditSetMaxPercentageState(
                        percentage = 95,
                        variationName = "Conventional Deadlift"
                    ),
                    liftMaxPercentage = EditSetMaxPercentageState(
                        percentage = 90,
                        variationName = "Deadlift"
                    )
                ),
                weight = 405.0,
                reps = 3,
                tempo = TempoState(
                    ecc = 3,
                    iso = 1,
                    con = 1
                ),
                notes = "PR attempt! Felt heavy but moved well.",
                date = LocalDate(2025, 10, 20).atStartOfDayIn(),
                showV2 = false
            )
        )
}

@Preview
@Composable
fun EditSetScreenPreview(
    @PreviewParameter(EditSetStateProvider::class) state: EditSetState,
) {
    PreviewAppTheme(isDarkMode = false) {
        EditSetScreen(
            state = state,
            sendEvent = {},
            showVariationDialog = {}
        )
    }
}

@Preview
@Composable
fun EditSetScreenDarkPreview(
    @PreviewParameter(EditSetStateProvider::class) state: EditSetState,
) {
    PreviewAppTheme(isDarkMode = true) {
        EditSetScreen(
            state = state,
            sendEvent = {},
            showVariationDialog = {}
        )
    }
}

@Preview
@Composable
fun EditSetScreenv2Preview(
    @PreviewParameter(EditSetStateProvider::class) state: EditSetState,
) {
    PreviewAppTheme(isDarkMode = false) {
        EditSetScreenV2(
            state = state,
            sendEvent = {},
            showVariationDialog = {}
        )
    }
}

@Preview
@Composable
fun EditSetScreenv2DarkPreview(
    @PreviewParameter(EditSetStateProvider::class) state: EditSetState,
) {
    PreviewAppTheme(isDarkMode = true) {
        EditSetScreenV2(
            state = state,
            sendEvent = {},
            showVariationDialog = {}
        )
    }
}
