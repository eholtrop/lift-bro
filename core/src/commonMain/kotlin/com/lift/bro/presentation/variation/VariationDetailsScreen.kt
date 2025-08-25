@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package com.lift.bro.presentation.variation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.workout.SetInfoRow
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.toString
import com.lift.bro.ui.Card
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.edit_daily_notes_dialog_confirm_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_dismiss_cta
import org.jetbrains.compose.resources.stringResource
import kotlin.collections.List
import kotlin.collections.forEach
import kotlin.collections.groupBy
import kotlin.collections.map
import kotlin.collections.maxOf
import kotlin.collections.sortedByDescending
import kotlin.collections.toList

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
    val variation by dependencies.database.variantDataSource.listen(variationId)
        .collectAsState(null)
    val sets = dependencies.database.setDataSource.getAll(variation?.id ?: "")

    variation?.let {
        VariationDetailsScreen(
            variation = it,
            sets = sets,
            addSetClicked = addSetClicked,
            setClicked = setClicked,
        )
    }
}

@Composable
private fun VariationDetailsScreen(
    variation: Variation,
    sets: List<LBSet>,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {

    var grouping by rememberSaveable { mutableStateOf(Grouping.Date) }

    LiftingScaffold(
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = "Add Set",
            fabClicked = addSetClicked,
        ),
        title = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(variation.fullName)

                Space(MaterialTheme.spacing.half)

                Text(
                    text = variation.maxText(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
    ) { padding ->


        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            item {
                var showNotesDialog by remember { mutableStateOf(false) }

                if (showNotesDialog) {
                    var variationNotes by remember(variation) { mutableStateOf(variation.notes) }
                    AlertDialog(
                        onDismissRequest = { showNotesDialog = false },
                        confirmButton = {
                            Button(
                                onClick = {
                                    GlobalScope.launch {
                                        dependencies.database.variantDataSource.save(
                                            variation = variation.copy(notes = variationNotes)
                                        )
                                        showNotesDialog = false
                                    }
                                }
                            ) {
                                Text(stringResource(Res.string.edit_daily_notes_dialog_confirm_cta))
                            }
                        },
                        dismissButton = {
                            Button(
                                onClick = {
                                    showNotesDialog = false
                                }
                            ) {
                                Text(stringResource(Res.string.edit_daily_notes_dialog_dismiss_cta))
                            }
                        },
                        title = {
                            Text(
                                text = "${variation.fullName} notes"
                            )
                        },
                        text = {
                            val focusRequester = FocusRequester()
                            TextField(
                                modifier = Modifier.defaultMinSize(minHeight = 128.dp)
                                    .focusRequester(focusRequester),
                                value = variationNotes ?: "",
                                placeholder = { Text("Squeeze your peach!\nBreath!\nKeep knees past toes") },
                                onValueChange = { variationNotes = it },
                            )

                            LaunchedEffect(Unit) {
                                focusRequester.requestFocus()
                            }
                        }
                    )
                }

                Row(
                    modifier = Modifier.clickable(
                        onClick = {
                            showNotesDialog = true
                        }
                    ).fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = variation.notes?.ifBlank { "No Notes" } ?: "No Notes",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Space(MaterialTheme.spacing.half)
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Notes",
                    )
                }
            }

            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row {
                        var expanded by rememberSaveable { mutableStateOf(false) }

                        Button(
                            onClick = { expanded = true }
                        ) {
                            Text(text = grouping.toString())
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            Grouping.values().forEach {
                                DropdownMenuItem(
                                    text = { Text(it.toString()) },
                                    onClick = {
                                        grouping = it
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

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


            items(items) { entry ->
                Card {
                    Column(
                        modifier = Modifier.padding(all = MaterialTheme.spacing.one)
                    ) {
                        Text(
                            text = entry.first,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Row(
                            modifier = Modifier.padding(
                                vertical = MaterialTheme.spacing.half
                            ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                entry.second.sortedByDescending { it.weight }
                                    .forEach { set ->
                                        SetInfoRow(
                                            modifier = Modifier.clickable(
                                                role = Role.Button,
                                                onClick = { setClicked(set) }
                                            ).fillMaxWidth().defaultMinSize(minHeight = 44.dp),
                                            set = set
                                        )
                                    }
                            }

                            Space(MaterialTheme.spacing.half)

                            Box(
                                modifier = Modifier.background(
                                    color = variation.lift?.color?.toColor()
                                        ?: MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                ).height(MaterialTheme.spacing.oneAndHalf).aspectRatio(1f),
                                content = {}
                            )
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
            contentDescription = "Down"
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
            contentDescription = "Up"
        )
        Text(
            text = up.toString(),
            style = MaterialTheme.typography.labelSmall,
        )
    }
}