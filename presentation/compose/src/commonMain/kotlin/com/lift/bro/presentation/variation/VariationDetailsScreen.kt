@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.lift.bro.presentation.variation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import com.lift.bro.ui.Card
import com.lift.bro.ui.CheckField
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.ui.transparentColors
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_details_fab_content_description
import lift_bro.core.generated.resources.variation_details_notes_label
import lift_bro.core.generated.resources.variation_details_notes_placeholder
import lift_bro.core.generated.resources.variation_details_screen_edit_title_content_description
import lift_bro.core.generated.resources.variation_details_tempo_down_cd
import lift_bro.core.generated.resources.variation_details_tempo_up_cd
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.listCorners
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.ktx.datetime.toLocalDate
import kotlin.time.Clock

private enum class Grouping {
    Date,
    Reps,
    Tempo,
    Weight
}

@Composable
fun VariationDetailsScreen(
    variationId: String,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    VariationDetailsScreen(
        interactor = rememberMovementDetailsInteractor(variationId),
        addSetClicked = addSetClicked,
        setClicked = setClicked,
    )
}

@Composable
private fun VariationDetailsScreen(
    interactor: MovementDetailsInteractor,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val state by interactor.state.collectAsState()
    var grouping by rememberSaveable { mutableStateOf(Grouping.Date) }
    var editName by rememberSaveable { mutableStateOf(false) }
    var name by remember(state.movement.name) { mutableStateOf(state.movement.name ?: "") }

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.lift_details_fab_content_description),
            fabClicked = addSetClicked,
        ),
        title = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (editName) {
                    var editedName by remember { mutableStateOf(name) }
                    TextField(
                        modifier = Modifier.wrapContentWidth(),
                        value = editedName,
                        textStyle = MaterialTheme.typography.headlineMedium,
                        supportingText = {
                            Text("Back Squat, Incline Bench, Bulgarian Skullcrushers....")
                        },
                        onValueChange = {
                            editedName = it
                        },
                        colors = TextFieldDefaults.transparentColors(),
                        suffix = {
                            IconButton(
                                onClick = {
                                    interactor(MovementDetailsEvent.UpdateMovement.NameUpdated(editedName))
                                    editName = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Save,
                                    contentDescription = "Save Movement"
                                )
                            }
                        }
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .clip(MaterialTheme.shapes.medium)
                            .clickable(
                                onClick = {
                                    editName = true
                                },
                                role = Role.Button
                            )
                            .padding(
                                horizontal = MaterialTheme.spacing.one
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                    ) {
                        Text(state.movement.name ?: "Create Movement")
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(
                                Res.string.variation_details_screen_edit_title_content_description
                            )
                        )
                    }
                }
            }
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            state.movement.name?.let {
                item {
                }

                item {
                    Card {
                        Column {
                            TextField(
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Default.Notes,
                                        contentDescription = null
                                    )
                                },
                                label = { Text(stringResource(Res.string.variation_details_notes_label)) },
                                placeholder = { Text(stringResource(Res.string.variation_details_notes_placeholder)) },
                                modifier = Modifier.fillMaxWidth(),
                                value = state.movement.notes ?: "",
                                onValueChange = { interactor(MovementDetailsEvent.UpdateMovement.NotesUpdated(it)) }
                            )

                            CheckField(
                                title = "Body Weight \uD83E\uDD38",
                                checked = state.movement.bodyWeight == true,
                                checkChanged = {
                                    interactor(
                                        MovementDetailsEvent.UpdateMovement.ToggleBodyWeight
                                    )
                                }
                            )
                        }
                    }
                }
            }

            val sets = state.cards.map { it.sets }.flatten()

            val items: List<Pair<String, List<LBSet>>> = when (grouping) {
                Grouping.Date -> sets.groupBy { it.date.toLocalDate() }.toList()
                    .sortedByDescending { it.first }
                    .map { Pair(it.first.toString("EEEE, MMM d"), it.second) }

                Grouping.Reps -> sets.groupBy { it.reps }.toList().sortedByDescending { it.first }
                    .map { Pair("${it.first} Rep(s)", it.second) }

                Grouping.Tempo -> sets.groupBy { it.tempo }.toList()
                    .sortedByDescending { it.second.maxOf { it.date.toLocalDate() } }
                    .map { Pair("${it.first.down}/${it.first.hold}/${it.first.up}", it.second) }

                Grouping.Weight -> sets.groupBy { it.weight }.toList()
                    .sortedByDescending { it.first }.map { Pair("${it.first}", it.second) }
            }

            if (items.isEmpty() && state.movement.name != null) {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Do you even lift bro?")
                        Text("Add a set with + below!")

                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = ""
                        )
                    }
                }
            } else {
                items(items) { entry ->
                    Card {
                        Column {
                            Text(
                                modifier = Modifier.padding(start = MaterialTheme.spacing.one),
                                text = entry.first,
                                style = MaterialTheme.typography.titleLarge
                            )

                            Space(MaterialTheme.spacing.half)

                            entry.second.sortedByDescending { it.weight }
                                .forEachIndexed { index, set ->
                                    val rowShape = MaterialTheme.shapes.small.listCorners(index, entry.second)
                                    SetInfoRow(
                                        modifier = Modifier
                                            .padding(horizontal = MaterialTheme.spacing.half)
                                            .clip(rowShape)
                                            .background(
                                                color = MaterialTheme.colorScheme.surfaceContainer,
                                                shape = rowShape,
                                            )
                                            .border(
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface),
                                                shape = rowShape,
                                            ).clickable(
                                                role = Role.Button,
                                                onClick = { setClicked(set) }
                                            )
                                            .fillMaxWidth()
                                            .padding(all = MaterialTheme.spacing.half),
                                        set = set
                                    )
                                }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun Tempo.render() {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(12.dp),
            imageVector = Icons.Default.KeyboardArrowDown,
            contentDescription = stringResource(Res.string.variation_details_tempo_down_cd)
        )
        Text(
            text = down.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
        Space(MaterialTheme.spacing.quarter)
        Text(
            text = "-",
            style = MaterialTheme.typography.labelSmall,
        )
        Text(
            text = hold.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
        Icon(
            modifier = Modifier.size(12.dp),
            imageVector = Icons.Default.KeyboardArrowUp,
            contentDescription = stringResource(Res.string.variation_details_tempo_up_cd)
        )
        Text(
            text = up.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}

class VariationDetailsStateProvider: PreviewParameterProvider<MovementDetailsState> {
    override val values: Sequence<MovementDetailsState>
        get() = sequenceOf(
            MovementDetailsState(
                movement = Movement(
                    lift = Category(
                        name = "Squat",
                        color = 0xFF2196F3uL
                    ),
                    name = "Back Squat",
                    favourite = true
                ),
                cards = emptyList()
            ),
            MovementDetailsState(
                movement = Movement(
                    lift = Category(
                        name = "Bench Press",
                        color = 0xFF4CAF50uL
                    ),
                    name = "Flat Bench",
                    favourite = true,
                    bodyWeight = false
                ),
                cards = listOf(
                    MovementDetailsCard(
                        title = "Monday, Jan 15",
                        sets = listOf(
                            LBSet(
                                id = "set1",
                                variationId = "var1",
                                weight = 225.0,
                                reps = 5,
                                rpe = 8,
                                date = Clock.System.now()
                            ),
                            LBSet(
                                id = "set2",
                                variationId = "var1",
                                weight = 245.0,
                                reps = 3,
                                rpe = 9,
                                date = Clock.System.now()
                            )
                        )
                    )
                )
            )
        )
}
