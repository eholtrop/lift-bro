@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.StickyNote2
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.logging.Log
import com.lift.bro.logging.d
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.Fade
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoInfoDialogText
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.calendar.Calendar
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.theme.aerospaceOrange
import com.lift.bro.ui.theme.amber
import com.lift.bro.ui.theme.orangePeel
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import com.lift.bro.utils.horizontal_padding.padding
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import com.lift.bro.utils.vertical_padding.padding
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.tempo_selector_dialog_title
import lift_bro.core.generated.resources.tempo_selector_with_tempo_text
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

enum class RPE(
    val rpe: Int,
    val emoji: String,
) {
    Five(5, "😇"),
    Six(6, "😀"),
    Seven(7, "💪"),
    Eight(8, "😰"),
    Nine(9, "😳"),
    Ten(10, "💀")
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

    state?.let { set ->
        EditSetScreen(
            state = set,
            sendEvent = { interactor(it) }
        )
    }
}

@Composable
fun EditSetScreen(
    state: EditSetState,
    sendEvent: (EditSetEvent) -> Unit,
) {
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            Text(
                stringResource(
                    if (state.id != null) Res.string.create_set_screen_title else Res.string.edit_set_screen_title
                )
            )
        },
        trailingContent = {
            Fade(visible = state.saveEnabled) {
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
                    onClick = {
                        sendEvent(EditSetEvent.DeleteSetClicked)
                    }
                )
            }
        },
    ) { padding ->

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.half,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            item {
                when {
                    state.variation == null -> {
                        Button(
                            colors = ButtonDefaults.outlinedButtonColors(),
                            onClick = {
                                showVariationDialog = true
                            }
                        ) {
                            Text(
                                "Select Variation",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.padding(
                                vertical = MaterialTheme.spacing.one,
                                horizontal = MaterialTheme.spacing.one
                            ).animateContentSize()
                        ) {
                            Column(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable(
                                        onClick = {
                                            showVariationDialog = true
                                        },
                                        role = Role.Button
                                    )
                                    .padding(
                                        vertical = MaterialTheme.spacing.quarter,
                                        horizontal = MaterialTheme.spacing.one
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = buildAnnotatedString {
                                        with(state.variation) {
                                            if (liftMaxPercentage == null && variationMaxPercentage == null) {
                                                withStyle(
                                                    MaterialTheme.typography.titleMedium
                                                        .copy(
                                                            color = MaterialTheme.colorScheme.primary,
                                                        ).toSpanStyle(),
                                                ) {
                                                    append(variation.fullName)
                                                }
                                            } else {
                                                variationMaxPercentage?.let {
                                                    append(
                                                        buildSetLiftTitle(
                                                            value = it.percentage,
                                                            name = it.variationName
                                                        )
                                                    )
                                                }

                                                liftMaxPercentage?.let {
                                                    if (variationMaxPercentage != null) {
                                                        appendLine()
                                                    }
                                                    append(
                                                        buildSetLiftTitle(
                                                            value = it.percentage,
                                                            name = it.variationName
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
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
                    Row(
                        modifier = Modifier.padding(start = MaterialTheme.spacing.one, top = MaterialTheme.spacing.one),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(Res.string.tempo_selector_with_tempo_text),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Space(MaterialTheme.spacing.half)
                        InfoDialogButton(
                            modifier = Modifier.size(16.dp),
                            dialogTitle = { Text(stringResource(Res.string.tempo_selector_dialog_title)) },
                            dialogMessage = { TempoInfoDialogText() }
                        )
                    }
                    TempoSelector(
                        modifier = Modifier
                            .padding(
                                horizontal = MaterialTheme.spacing.half,
                                vertical = MaterialTheme.spacing.half,
                            )
                            .background(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = MaterialTheme.shapes.small,
                            )
                            .border(
                                color = MaterialTheme.colorScheme.onSurface,
                                width = 1.dp,
                                shape = MaterialTheme.shapes.small
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

    if (showVariationDialog) {
        VariationSearchDialog(
            visible = showVariationDialog,
            textFieldPlaceholder = stringResource(Res.string.edit_set_screen_variation_selector_empty_state_title),
            onDismissRequest = { showVariationDialog = false },
            onVariationSelected = {
                showVariationDialog = false
                sendEvent(EditSetEvent.VariationSelected(it))
            }
        )
    }
}

@Composable
fun EditSetScreenV2(
    state: EditSetState,
    sendEvent: (EditSetEvent) -> Unit,
) {
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            Text(
                stringResource(
                    if (state.id != null) Res.string.create_set_screen_title else Res.string.edit_set_screen_title
                )
            )
        },
        trailingContent = {
            Fade(visible = state.saveEnabled) {
                TopBarIconButton(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
                    onClick = {
                        sendEvent(EditSetEvent.DeleteSetClicked)
                    }
                )
            }
        },
    ) { padding ->

        var showRpe by remember { mutableStateOf(state.rpe != null) }
        var showNotes by remember { mutableStateOf(state.notes.isNotBlank()) }
        var showTempo by remember { mutableStateOf(state.tempo != TempoState()) }
        var showDate by remember { mutableStateOf(false) }

        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxHeight(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                horizontal = MaterialTheme.spacing.half,
            ),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            item {
                when {
                    state.variation == null -> {
                        Button(
                            colors = ButtonDefaults.outlinedButtonColors(),
                            onClick = {
                                showVariationDialog = true
                            }
                        ) {
                            Text(
                                "Select Variation",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier = Modifier.padding(
                                vertical = MaterialTheme.spacing.one,
                                horizontal = MaterialTheme.spacing.one
                            ).animateContentSize()
                        ) {
                            val weight = state.weight ?: 0.0
                            Column(
                                modifier = Modifier
                                    .clip(MaterialTheme.shapes.medium)
                                    .clickable(
                                        onClick = {
                                            showVariationDialog = true
                                        },
                                        role = Role.Button
                                    )
                                    .padding(
                                        vertical = MaterialTheme.spacing.quarter,
                                        horizontal = MaterialTheme.spacing.one
                                    ),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = buildAnnotatedString {
                                        Log.d(message = state.variation.toString())
                                        with(state.variation) {
                                            if (liftMaxPercentage == null && variationMaxPercentage == null) {
                                                withStyle(
                                                    MaterialTheme.typography.titleMedium
                                                        .copy(
                                                            color = MaterialTheme.colorScheme.primary,
                                                        ).toSpanStyle(),
                                                ) {
                                                    append(variation.fullName)
                                                }
                                            } else {
                                                variationMaxPercentage?.let {
                                                    append(
                                                        buildSetLiftTitle(
                                                            value = it.percentage,
                                                            name = it.variationName
                                                        )
                                                    )
                                                }

                                                liftMaxPercentage?.let {
                                                    if (variationMaxPercentage != null) {
                                                        appendLine()
                                                    }
                                                    append(
                                                        buildSetLiftTitle(
                                                            value = it.percentage,
                                                            name = it.variationName
                                                        )
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            item {
                val bottomPadding = if (showNotes && showRpe && showTempo && showDate) MaterialTheme.spacing.half else MaterialTheme.spacing.one
                Box {
                    Column(
                        modifier = Modifier
                            .padding(bottom = bottomPadding)
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
                                bottom = bottomPadding,
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
                        )

                        AnimatedVisibility(showRpe) {
                            var rpe by remember { mutableStateOf(state.rpe ?: 7) }
                            Column {
                                InfoDialogButton(
                                    dialogTitle = { Text("Rpe") },
                                    dialogMessage = { }
                                ) {
                                    Text(
                                        text = "RPE",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.onBackground
                                    )
                                }
                                RpeBar(
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
                        ChipButton(
                            visible = !showRpe,
                            onClick = { showRpe = true }
                        ) {
                            Text("RPE+")
                        }
                        ChipButton(
                            visible = !showTempo,
                            onClick = { showTempo = true }
                        ) {
                            if (state.tempo == TempoState()) {
                                Text("Tempo")
                            } else {
                                Text("${state.tempo.ecc}/${state.tempo.iso}/${state.tempo.con}")
                            }
                        }
                        Space()
                        ChipButton(
                            visible = !showNotes,
                            onClick = { showNotes = true }
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Default.StickyNote2,
                                contentDescription = "Add Notes"
                            )
                        }
                        ChipButton(
                            visible = !showDate,
                            onClick = { showDate = true }
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = state.date.toString("MMM d"))
                            }
                        }
                    }
                }
            }

            if (showNotes) {
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
                }
            }

            if (showDate) {
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

    if (showVariationDialog) {
        VariationSearchDialog(
            visible = showVariationDialog,
            textFieldPlaceholder = stringResource(Res.string.edit_set_screen_variation_selector_empty_state_title),
            onDismissRequest = { showVariationDialog = false },
            onVariationSelected = {
                showVariationDialog = false
                sendEvent(EditSetEvent.VariationSelected(it))
            }
        )
    }
}

@Composable
fun RpeBar(
    modifier: Modifier = Modifier,
    rpe: Int,
    rpeChanged: (Int) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier.background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color.Transparent,
                        amber,
                        orangePeel,
                        aerospaceOrange
                    )
                ),
            ).fillMaxWidth()
                .height(MaterialTheme.spacing.quarter)
        ) {
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RPE.values().forEach {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (it.rpe == rpe) {
                        Text(it.emoji, style = MaterialTheme.typography.headlineMedium)
                    } else {
                        Button(
                            modifier = Modifier.size(MaterialTheme.spacing.two),
                            onClick = {
                                rpeChanged(it.rpe)
                            },
                            colors = ButtonDefaults.textButtonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.onBackground,
                            ),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(it.rpe.toString())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun buildSetLiftTitle(
    value: Int,
    name: String,
): AnnotatedString {
    return buildAnnotatedString {
        val str = stringResource(
            Res.string.weight_selector_chin_title,
            value,
            name,
        )

        append(
            str.take(str.indexOf(name))
        )

        withStyle(
            MaterialTheme.typography.titleMedium
                .copy(
                    color = MaterialTheme.colorScheme.primary,
                ).toSpanStyle(),
        ) {
            append(
                name,
            )
        }

        append(
            str.substring(
                str.indexOf(name) + name.length,
            )
        )
    }
}

@Composable
private fun ChipButton(
    onClick: () -> Unit,
    visible: Boolean,
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
            contentPadding = PaddingValues(0.dp),
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
                date = Clock.System.now()
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
                )
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
                )
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
            sendEvent = {}
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
            sendEvent = {}
        )
    }
}
