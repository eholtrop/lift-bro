package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lift.bro.di.dependencies
import comliftbrodb.Lift
import comliftbrodb.Variation
import spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VariationSelector(
    modifier: Modifier = Modifier,
    variation: Variation?,
    variationSelected: (Variation) -> Unit,
) {
    var showSelector by remember { mutableStateOf(false) }

    if (showSelector) {

        AlertDialog(
            modifier = Modifier.background(
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.medium,
            ),
            onDismissRequest = { showSelector = false },
        ) {
            val lifts by dependencies.database.liftDataSource.getAll()
                .collectAsState(emptyList())

            Column {
                Spacer(modifier = Modifier.padding(top = MaterialTheme.spacing.one))
                lifts.forEach { lift ->

                    Text(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        text = lift.name,
                    )

                    val variations =
                        dependencies.database.variantDataSource.getAll(lift.id).executeAsList()

                    LazyVerticalGrid(
                        columns = GridCells.Adaptive(96.dp),
                        contentPadding = PaddingValues(MaterialTheme.spacing.one)
                    ) {
                        items(variations) { variation ->
                            VariationCard(
                                modifier = Modifier.padding(MaterialTheme.spacing.quarter)
                                    .defaultMinSize(96.dp, 96.dp),
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