@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.SharedTransitionScope
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
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.presentation.set.components.EditSetVariationSelector
import com.lift.bro.ui.Fade
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.RpeSelector
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.calendar.Calendar
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.tempo_selector_timer_content_description
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import tv.dpal.compose.padding.vertical.padding
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
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
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            state?.let {
                Text(
                    stringResource(
                        if (it.id != null) Res.string.create_set_screen_title else Res.string.edit_set_screen_title
                    )
                )
            }
        },
        trailingContent = {
            Fade(visible = state?.saveEnabled == true) {
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
                    onClick = {
                        interactor(EditSetEvent.DeleteSetClicked)
                    }
                )
            }

            Switch(
                checked = state?.showV2 == true,
                onCheckedChange = { interactor(EditSetEvent.ToggleV2) },
                thumbContent = {
                    Text(
                        if (state?.showV2 == true) "V2" else "V1",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            )
            if (dependencies.settingsRepository.enableTimer()) {
                val navCoordinator = LocalNavCoordinator.current
                val tempo = state?.let { Tempo(down = it.tempo.ecc ?: 3, hold = it.tempo.iso ?: 1, up = it.tempo.con ?: 1) } ?: Tempo()
                IconButton(
                    onClick = {
                        navCoordinator.present(Destination.Timer(reps = state?.reps?.toInt() ?: 1, tempo = tempo))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = stringResource(Res.string.tempo_selector_timer_content_description)
                    )
                }
            }
        },
    ) { padding ->
        state?.let { set ->
            if (set.showV2) {
                EditSetScreenV2(
                    modifier = Modifier.padding(padding),
                    state = set,
                    sendEvent = { interactor(it) },
                    showVariationDialog = {
                        showVariationDialog = true
                    }
                )
            } else {
                EditSetScreen(
                    modifier = Modifier.padding(padding),
                    state = set,
                    sendEvent = { interactor(it) },
                    showVariationDialog = {
                        showVariationDialog = true
                    }
                )
            }
        }
    }

    if (showVariationDialog) {
        VariationSearchDialog(
            visible = showVariationDialog,
            textFieldPlaceholder = stringResource(Res.string.edit_set_screen_variation_selector_empty_state_title),
            onDismissRequest = { showVariationDialog = false },
            onVariationSelected = {
                showVariationDialog = false
                interactor(EditSetEvent.VariationSelected(it))
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
                        Text(stringResource(Res.string.edit_set_screen_extra_notes_placeholder))
                    },
                    label = {
                        Text(stringResource(Res.string.edit_set_screen_extra_notes_label))
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
) {
    var showRpe by remember { mutableStateOf(false) }
    var showTempo by remember { mutableStateOf(false) }

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
                            .padding(bottom = if (showRpe && showTempo) 0.dp else MaterialTheme.spacing.one)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.surface,
                                        Color.Transparent,
                                    ),
                                ),
                                shape = MaterialTheme.shapes.medium,
                            ).padding(
                                horizontal = MaterialTheme.spacing.one,
                                top = MaterialTheme.spacing.half,
                                bottom = if (showRpe && showTempo) {
                                    MaterialTheme.spacing.half
                                } else {
                                    MaterialTheme.spacing.one
                                },
                            ),
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

                        AnimatedVisibility(showRpe) {
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

                        AnimatedVisibility(showTempo) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                TempoSelector(
                                    tempo = state.tempo,
                                    tempoChanged = { sendEvent(EditSetEvent.TempoChanged(it)) }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .animateContentSize()
                            .align(Alignment.BottomStart)
                            .padding(horizontal = MaterialTheme.spacing.half),
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        Space()
                        ChipButton(
                            visible = !showRpe,
                            onClick = { showRpe = true },
                            transitionKey = "rpe",
                            sharedTransitionScope = this@SharedTransitionLayout,
                        ) {
                            RPE.entries.firstOrNull { state.rpe == it.rpe }?.let {
                                Text("${it.emoji} RPE: ${it.rpe}")
                            } ?: run {
                                Text("RPE+")
                            }
                        }
                        ChipButton(
                            visible = !showTempo,
                            onClick = { showTempo = true },
                            transitionKey = "tempo",
                            sharedTransitionScope = this@SharedTransitionLayout,
                        ) {
                            if (state.tempo == TempoState()) {
                                Text("Tempo")
                            } else {
                                Text("Tempo: ${state.tempo.ecc}/${state.tempo.iso}/${state.tempo.con}")
                            }
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
                            Text(stringResource(Res.string.edit_set_screen_extra_notes_placeholder))
                        },
                        label = {
                            Text(stringResource(Res.string.edit_set_screen_extra_notes_label))
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
private fun ChipButton(
    onClick: () -> Unit,
    visible: Boolean,
    transitionKey: String,
    sharedTransitionScope: SharedTransitionScope,
    content: @Composable RowScope.() -> Unit,
) {
    AnimatedVisibility(
        visible,
        exit = fadeOut(),
    ) {
        with(sharedTransitionScope) {
            Button(
                modifier = Modifier.height(24.dp)
                    .sharedElement(
                        rememberSharedContentState(transitionKey),
                        animatedVisibilityScope = this@AnimatedVisibility
                    ),
                onClick = onClick,
                content = content,
                contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.quarter),
                shape = MaterialTheme.shapes.small,
            )
        }
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
                date = Clock.System.now(),
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
