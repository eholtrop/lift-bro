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
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.excercise.SetInfoRow
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.toString
import com.lift.bro.ui.Card
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.utils.formattedMax
import com.lift.bro.utils.formattedWeight
import com.lift.bro.utils.toColor
import com.lift.bro.utils.toLocalDate
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
    val variation = dependencies.database.variantDataSource.get(variationId)
    val lift = variation?.lift
    val sets = dependencies.database.setDataSource.getAll(variation?.id ?: "")

    variation?.let {
        VariationDetailsScreen(
            variation = variation,
            lift = lift,
            sets = sets,
            addSetClicked = addSetClicked,
            setClicked = setClicked,
        )
    }
}

@Composable
private fun VariationDetailsScreen(
    variation: Variation,
    lift: Lift?,
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
        title = "${variation.name} ${lift?.name}",
    ) { padding ->


        LazyColumn(
            modifier = Modifier.padding(padding),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {

            stickyHeader {
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