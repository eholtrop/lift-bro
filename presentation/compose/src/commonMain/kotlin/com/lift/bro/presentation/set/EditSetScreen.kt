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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.set.components.EditSetVariationSelector
import com.lift.bro.presentation.video.VideoPlayer
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
import com.lift.bro.ui.transparentColors
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.underlineRange
import kotlinx.datetime.LocalDate
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.ktx.datetime.atStartOfDayIn
import tv.dpal.ktx.datetime.toLocalDate
import kotlin.time.Instant

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
    sectionId: String?,
) {
    EditSetScreen(
        interactor = rememberCreateSetInteractor(
            movementId = variationId,
            date = date,
            sectionId = sectionId,
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
    val navCoordinator = LocalNavCoordinator.current
    var showVariationDialog by remember { mutableStateOf(false) }

    LiftingScaffold(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                var textLayoutResult by remember(state.failureRep) { mutableStateOf<TextLayoutResult?>(null) }
                val titleCta by remember(
                    state.failureRep
                ) { mutableStateOf(if (state.failureRep != null) "Attempted" else "Crushed") }
                val title = buildEditSetScreenTitle(
                    titleCta,
                    onClick = {
                        onEvent(EditSetEvent.ToggleSetFailed)
                    }
                )
                val lineColor = MaterialTheme.colorScheme.primary

                Text(
                    modifier = Modifier.underlineRange(
                        textLayoutResult = textLayoutResult,
                        start = 2,
                        end = title.length,
                        lineColor = lineColor,
                    ),
                    onTextLayout = { textLayoutResult = it },
                    text = title,
                )
                Space(MaterialTheme.spacing.half)
                IconButton(
                    onClick = {
                        navCoordinator.present(Destination.Recording(state.id))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Videocam,
                        contentDescription = strings.timerContentDescription
                    )
                }
            }
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

            if (state.timerEnabled) {
                val navCoordinator = LocalNavCoordinator.current
                val tempo = state.let {
                    Tempo(
                        down = it.tempo.ecc ?: 3,
                        hold = it.tempo.iso ?: 1,
                        up = it.tempo.con ?: 1
                    )
                }
            }
        },
    ) { padding ->
        EditSetScreen(
            modifier = Modifier.padding(padding),
            state = state,
            sendEvent = { onEvent(it) },
            showVariationDialog = {
                showVariationDialog = true
            },
            strings = strings,
        )
    }

    LaunchedEffect(state.id) {
        if (state.movement == null) {
            showVariationDialog = true
        }
    }

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

@Composable
private fun buildEditSetScreenTitle(
    titleCta: String,
    onClick: () -> Unit,
): AnnotatedString = buildAnnotatedString {
    append("I ")
    withLink(
        LinkAnnotation.Clickable(
            tag = "Toggle Failure",
            styles = TextLinkStyles(
                style = LocalTextStyle.current.toSpanStyle()
            ),
            linkInteractionListener = {
                onClick()
            }
        )
    ) {
        append(
            titleCta
        )
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun EditSetScreen(
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

                        AnimatedVisibility(
                            visible = state.failureRep != null
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                            ) {
                                Column {
                                    Text(
                                        text = "But I failed on Rep",
                                        style = MaterialTheme.typography.titleLarge
                                    )

                                    AnimatedVisibility(
                                        visible = state.showFailedRepsOutOfBoundsError
                                    ) {
                                        Text(
                                            text = "Cannot be 0 or greater than ${state.reps}",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                }

                                Space(MaterialTheme.spacing.half)

                                RepWeightTextField(
                                    value = state.failureRep.toString(),
                                    onValueChanged = {
                                        it.toLongOrNull()?.let {
                                            sendEvent(EditSetEvent.FailureRepChanged(rep = it))
                                        }
                                    },
                                    error = state.showFailedRepsOutOfBoundsError,
                                    keyboardType = KeyboardType.Number,
                                )
                            }
                        }

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
                        modifier = Modifier.fillMaxWidth().testTag("notes"),
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
                        ).clip(MaterialTheme.shapes.medium),
                    horizontalAlignment = Alignment.CenterHorizontally,
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
                                text = state.date.toString(strings.dateMonthDayFormat),
                                color = if (showCalendar) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.date.toString(strings.dateYearFormat),
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
                                text = state.date.toString(strings.dateTimeFormat),
                                color = MaterialTheme.colorScheme.onBackground,
                                style = MaterialTheme.typography.titleMedium,
                            )
                            Text(
                                text = state.date.toString(strings.dateAmpmFormat).lowercase(),
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
                    AnimatedVisibility(!showCalendar) {
                        when (val workout = state.workout) {
                            null -> {
                                Button(
                                    onClick = {
                                    },
                                    colors = ButtonDefaults.elevatedButtonColors()
                                ) {
                                    Text("Start a workout!")
                                }
                            }

                            else -> {
                                var selectedSectionId by remember { mutableStateOf(state.sectionId) }

                                Column(
                                    modifier = Modifier.padding(
                                        horizontal = MaterialTheme.spacing.one,
                                    )
                                ) {
                                    workout.exercises.forEachIndexed { index, exercise ->
                                        ExerciseCard(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(
                                                    vertical = MaterialTheme.spacing.quarter
                                                ),
                                            title = index.let { 'A'.plus(index).plus(".") },
                                            exercise = exercise,
                                            selectedSectionId = state.sectionId,
                                            onSectionSelected = {
                                                selectedSectionId = it.id
                                                sendEvent(
                                                    EditSetEvent.SectionSelected(it)
                                                )
                                            },
                                        )
                                        if (index != workout.exercises.lastIndex) {
                                            HorizontalDivider(
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                    Space(MaterialTheme.spacing.half)
                                }
                            }
                        }
                    }
                }
            }

            state.videoUri?.let { uri ->
                item {
                    val video = dependencies.videoStorage.getVideoFile(uri)
                    if (video != null) {
                        VideoPlayer(
                            modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                            videoFile = video,
                        )
                    }
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun ExerciseCard(
    modifier: Modifier = Modifier,
    exercise: Exercise,
    title: String,
    selectedSectionId: String?,
    onSectionSelected: (Section) -> Unit,
) {
    Row(
        modifier = modifier,
    ) {
        Text(
            modifier = Modifier.fillMaxHeight(),
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        Space(MaterialTheme.spacing.half)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter),
        ) {
            exercise.sections.forEachIndexed { index, section ->
                SectionChip(
                    section = section,
                    selected = section.id == selectedSectionId,
                    onClick = {
                        onSectionSelected(section)
                    }
                )
            }
        }
    }
}

@Composable
fun SectionChip(
    modifier: Modifier = Modifier,
    selected: Boolean,
    section: Section,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
    ) {
        ChipButton(
            onClick = onClick,
            colors = if (selected) ButtonDefaults.buttonColors() else ButtonDefaults.outlinedButtonColors(),
        ) {
            Text(section.primaryMovement?.name ?: "")
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ChipButton(
    onClick: () -> Unit,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    visible: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    AnimatedVisibility(
        visible,
        exit = fadeOut(),
    ) {
        OutlinedButton(
            modifier = Modifier.height(24.dp),
            onClick = onClick,
            content = content,
            colors = colors,
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.quarter),
            shape = MaterialTheme.shapes.small,
        )
    }
}

class EditSetStateProvider: PreviewParameterProvider<EditSetState> {
    override val values: Sequence<EditSetState>
        get() = sequenceOf(
            // New set with variation but no data
            EditSetState(
                id = "",
                movement = SetVariation(
                    Movement(
                        lift = Category(
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
            ),
            // Partially filled set
            EditSetState(
                id = "set1",
                movement = SetVariation(
                    Movement(
                        lift = Category(
                            name = "Bench Press",
                            color = 0xFF4CAF50uL
                        ),
                        name = "Flat Bench"
                    )
                ),
                weight = 225.0,
                reps = 5,
                failureRep = 3,
                rpe = null,
                notes = "",
                tempo = TempoState(
                    ecc = 3,
                    iso = 1,
                    con = 1
                ),
                date = LocalDate(2025, 10, 20).atStartOfDayIn(),
            ),
            // Complete set with all data
            EditSetState(
                id = "set2",
                movement = SetVariation(
                    Movement(
                        lift = Category(
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
