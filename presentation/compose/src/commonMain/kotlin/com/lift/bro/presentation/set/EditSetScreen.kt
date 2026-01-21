@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.set

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Start
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
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.Fade
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.calendar.Calendar
import com.lift.bro.ui.dialog.VariationSearchDialog
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import com.lift.bro.utils.fullName
import com.lift.bro.utils.horizontal_padding.padding
import com.lift.bro.utils.listCorners
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.weight_selector_chin_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider

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
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            item {
                RepWeightSelector(
                    modifier = Modifier.fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    Color.Transparent,
                                ),
                            ),
                            shape = MaterialTheme.shapes.medium.copy(
                                bottomEnd = CornerSize(0.dp),
                                bottomStart = CornerSize(0.dp),
                            ),
                        ).padding(
                            vertical = MaterialTheme.spacing.half,
                            horizontal = MaterialTheme.spacing.one,
                        ),
                    repChanged = { sendEvent(EditSetEvent.RepChanged(it)) },
                    weightChanged = { sendEvent(EditSetEvent.WeightChanged(it)) },
                    rpeChanged = { sendEvent(EditSetEvent.RpeChanged(it)) },
                    weight = state.weight,
                    reps = state.reps,
                    rpe = state.rpe,
                )
            }

            item {
                when {
                    state.variation == null -> {
                        Button(
                            colors = ButtonDefaults.outlinedButtonColors(),
                            onClick = {
                                showVariationDialog = true
                            }
                        ) {
                            Text("Select Variation")
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
                                        with(state.variation) {
                                            if (liftMaxPercentage != null && variationMaxPercentage != null) {
                                                append(variation.fullName)
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Box(
                        modifier = Modifier.background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.surface,
                                    Color.Transparent,
                                ),
                                startY = Float.POSITIVE_INFINITY,
                                endY = 0f
                            ),
                            shape = MaterialTheme.shapes.medium.copy(
                                topEnd = CornerSize(0.dp),
                                topStart = CornerSize(0.dp),
                            ),
                        )
                    ) {
                        TempoSelector(
                            tempo = state.tempo,
                            tempoChanged = { sendEvent(EditSetEvent.TempoChanged(it)) }
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
                        )
                        .padding(
                            vertical = MaterialTheme.spacing.half,
                            horizontal = MaterialTheme.spacing.one
                        )
                ) {
                    var date by remember(state.date) { mutableStateOf(state.date.toLocalDate()) }



                    Calendar(
                        modifier = Modifier.fillMaxWidth(),
                        selectedDate = date,
//                    contentPadding =,
//                    pagerState =,
//                    horizontalArrangement =,
//                    verticalArrangement =,
                        dateDecorations = { date, content ->
                            content()

                            if (date == state.date.toLocalDate()) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = ""
                                )
                            }
                        },
                        dateSelected = {
                            date = it
                        },
//                    contentForMonth =
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Text(
                                textAlign = TextAlign.Start,
                                text = state.date.toString("EEE, MMM d"),
                            )
                            Space()
                            Text(
                                textAlign = TextAlign.Start,
                                text = state.date.toString("yyyy"),
                            )
                        }

                        Space()
                        AnimatedVisibility(
                            visible = date != state.date.toLocalDate()
                        ) {
                            Button(
                                onClick = {
                                    sendEvent(EditSetEvent.DateSelected(date.atStartOfDayIn(TimeZone.currentSystemDefault())))
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Date"
                                )
                            }
                        }
                    }
                    val sets by dependencies.setRepository.listenAll(
                        startDate = date,
                        endDate = date,
                    ).map {
                        if (state.toDomain() != null) {
                            it + state.toDomain()!!
                        } else {
                            it
                        }
                    }.collectAsState(emptySet())

                    val variations by dependencies.variationRepository.listenAll()
                        .collectAsState(emptyList())

                    val varSets = sets.groupBy { set -> variations.firstOrNull { it.id == set.variationId } }


                    Column(
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                    ) {
                        varSets.toList()
                            .sortedByDescending { it.first?.fullName }
                            .sortedByDescending { it.first?.favourite }
                            .sortedByDescending { it.first?.id == state.variation?.variation?.id }
                            .forEach { (variation, sets) ->
                                Column(
                                    modifier = Modifier.background(
                                        brush = Brush.linearGradient(
                                            colors = listOf(
                                                when {
//                                                    variation?.id == state.variation?.variation?.id -> MaterialTheme.colorScheme.primary
                                                    variation?.lift?.color != null -> variation.lift?.color!!.toColor()
                                                    else -> MaterialTheme.colorScheme.surface
                                                },
                                                Color.Transparent
                                            )
                                        ),
                                        shape = MaterialTheme.shapes.medium,
                                    ).clip(MaterialTheme.shapes.medium)
                                        .padding(
                                            vertical = MaterialTheme.spacing.half
                                        )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(
                                            start = MaterialTheme.spacing.one,
                                        )
                                    ) {
                                        Text(
                                            text = variation?.fullName ?: ""
                                        )
                                        if (variation?.favourite == true) {
                                            Icon(
                                                imageVector = Icons.Default.Favorite,
                                                contentDescription = "Favourite"
                                            )
                                        }
                                    }

                                    sets.sortedByDescending { it.date }.distinctBy { it.id }.forEachIndexed { index, set ->
                                        SetInfoRow(
                                            modifier = Modifier.fillMaxWidth()
                                                .padding(
                                                    horizontal = MaterialTheme.spacing.half
                                                )
                                                .background(
                                                    color = MaterialTheme.colorScheme.surfaceContainer,
                                                    shape = MaterialTheme.shapes.small.listCorners(index, sets.distinctBy { it.id })
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = MaterialTheme.colorScheme.onBackground,
                                                    shape = MaterialTheme.shapes.small.listCorners(index, sets.distinctBy { it.id })
                                                )
                                                .padding(
                                                    vertical = MaterialTheme.spacing.quarter,
                                                    horizontal = MaterialTheme.spacing.half
                                                ),
                                            set = set,
                                            trailing = {
                                                if (set.id == state.id) {
                                                    Icon(
                                                        imageVector = Icons.Default.Star,
                                                        contentDescription = "This Set"
                                                    )
                                                }
                                                if (date != set.date.toLocalDate()) {
                                                    Icon(
                                                        imageVector = Icons.Default.Save,
                                                        contentDescription = "Save Date"
                                                    )
                                                }
                                            }
                                        )
                                    }
                                }
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
            str.substring(0, str.indexOf(name))
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

class EditSetStateProvider: PreviewParameterProvider<EditSetState> {
    override val values: Sequence<EditSetState>
        get() = sequenceOf(
            // New set - no variation selected yet
            EditSetState(
                id = null,
                variation = null,
                weight = null,
                reps = null,
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
