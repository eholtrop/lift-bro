@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.lift.bro.presentation.movement

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.ui.Card
import com.lift.bro.ui.CheckField
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.SubmitTextField
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.lift_details_fab_content_description
import lift_bro.core.generated.resources.variation_details_notes_label
import lift_bro.core.generated.resources.variation_details_notes_placeholder
import lift_bro.core.generated.resources.variation_details_tempo_down_cd
import lift_bro.core.generated.resources.variation_details_tempo_up_cd
import lift_bro.core.generated.resources.movement_details_body_weight
import lift_bro.core.generated.resources.movement_details_create_movement
import lift_bro.core.generated.resources.movement_details_empty_subtitle
import lift_bro.core.generated.resources.movement_details_empty_title
import lift_bro.core.generated.resources.movement_details_reps
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.listCorners
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.rememberInteractor
import tv.dpal.ktx.datetime.toLocalDate

private enum class Grouping {
    Date,
    Reps,
    Tempo,
    Weight
}

@Composable
fun CreateMovementScreen(
    movementId: String,
    categoryId: String? = null,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    MovementDetailsScreen(
        interactor = rememberMovementDetailsInteractor(
            movementId = movementId,
            categoryId = categoryId
        ),
        addSetClicked = addSetClicked,
        setClicked = setClicked,
    )
}

@Composable
fun MovementDetailsScreen(
    movementId: String,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    MovementDetailsScreen(
        interactor = rememberMovementDetailsInteractor(movementId = movementId),
        addSetClicked = addSetClicked,
        setClicked = setClicked,
    )
}

@Composable
private fun MovementDetailsScreen(
    interactor: MovementDetailsInteractor,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val state by interactor.state.collectAsState()
    var grouping by rememberSaveable { mutableStateOf(Grouping.Date) }
    var name by remember(state.movement.name) { mutableStateOf(state.movement.name ?: "") }

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.lift_details_fab_content_description),
            fabClicked = addSetClicked,
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                SubmitTextField(
                    value = name,
                    onValueSubmitted = {
                        interactor(MovementDetailsEvent.UpdateMovement.NameUpdated(it))
                    },
                    placeholder = {
                        Text(stringResource(Res.string.movement_details_create_movement))
                    }
                )
                if (state.movement.name != null) {
                    CategoryDropDown(
                        selectedCategory = state.movement.lift,
                        categoryChanged = {
                            interactor(MovementDetailsEvent.UpdateMovement.UpdateCategory(it.id))
                        }
                    )
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
                                title = stringResource(Res.string.movement_details_body_weight),
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
                        Text(stringResource(Res.string.movement_details_empty_title))
                        Text(stringResource(Res.string.movement_details_empty_subtitle))

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

@Preview
@Composable
fun VariationDetailsScreenPreview(
    @PreviewParameter(MovementDetailsStateProvider::class) state: MovementDetailsState,
) {
    PreviewAppTheme(isDarkMode = false) {
        MovementDetailsScreen(
            interactor = rememberInteractor(initialState = state),
            addSetClicked = {},
            setClicked = {},
        )
    }
}
