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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.fullName
import com.lift.bro.domain.models.maxText
import com.lift.bro.presentation.Interactor
import com.lift.bro.ui.Card
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.SetInfoRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
import com.lift.bro.utils.toString

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
        interactor = rememberVariationDetailInteractor(variationId),
        addSetClicked = addSetClicked,
        setClicked = setClicked,
    )
}

@Composable
private fun VariationDetailsScreen(
    interactor: Interactor<VariationDetailsState, VariationDetailsEvent>,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val state by interactor.state.collectAsState()

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
                Text(state.variation.fullName)

                Space(MaterialTheme.spacing.half)

                Text(
                    text = state.variation.maxText(),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        },
    ) { padding ->


        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {

                if (state.variation.notes != null) {
                    TextField(
                        label = { Text("Notes") },
                        placeholder = { Text("Toes pointed out, Squeeze core...") },
                        modifier = Modifier.fillMaxWidth(),
                        value = state.variation.notes ?: "",
                        onValueChange = { interactor(VariationDetailsEvent.NotesUpdated(it)) }
                    )
                } else {
                    Button(
                        onClick = {
                            interactor(VariationDetailsEvent.NotesUpdated(""))
                        },
                        colors = ButtonDefaults.elevatedButtonColors()
                    ) {
                        Text("Add Notes")
                    }
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
                                    color = state.variation.lift?.color?.toColor()
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