package com.lift.bro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lift.bro.di.dependencies
import comliftbrodb.Lift
import comliftbrodb.Variation
import spacing

@Composable
fun VariationSelector(
    modifier: Modifier = Modifier,
    variation: Variation?,
    variationSelected: (Variation) -> Unit,
) {
    var showSelector by remember { mutableStateOf(false) }

    if (showSelector) {

        Dialog(
            onDismissRequest = { showSelector = false }
        ) {

            Column {

                var parentDropdownExpanded by remember { mutableStateOf(false) }

                var parentLiftId by remember { mutableStateOf(variation?.liftId) }
                DropdownMenu(
                    modifier = Modifier.clickable { parentDropdownExpanded = true },
                    expanded = parentDropdownExpanded,
                    onDismissRequest = { parentDropdownExpanded = false },
                ) {

                    val lifts by dependencies.database.liftDataSource.getAll()
                        .collectAsState(emptyList())

                    lifts.forEach { lift ->
                        DropdownMenuItem(
                            text = {
                                Text(text = lift.name)
                            },
                            onClick = {
                                parentDropdownExpanded = false
                                parentLiftId = lift.id
                            }
                        )
                    }
                }

                parentLiftId?.let { liftId ->
                    val variations = dependencies.database.variantDataSource.getAll(liftId).executeAsList()

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(96.dp),
                        contentPadding = PaddingValues(MaterialTheme.spacing.one)
                    ) {
                        items(variations) { variation ->
                            VariationCard(
                                modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                                variation = variation,
                                onClick = {
                                    showSelector = false
                                    variationSelected(it)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    when (variation) {
        null -> {
            Card(
                modifier = modifier.size(96.dp, 96.dp),
                onClick = { showSelector = true }
            ) {
                Text(
                    text = "Select Variation",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        else -> {
            VariationCard(
                modifier = modifier.defaultMinSize(96.dp, 96.dp),
                variation = variation,
                onClick = { showSelector = true }
            )
        }
    }
}