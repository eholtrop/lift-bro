package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
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
import spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiftSelector(
    lift: Lift?,
    liftSelected: (Lift) -> Unit,
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
            val lifts by dependencies.database.liftDataSource.getAll().collectAsState(emptyList())

            LazyVerticalGrid(
                columns = GridCells.Adaptive(96.dp),
                contentPadding = PaddingValues(MaterialTheme.spacing.one)
            ) {
                items(lifts) { lift ->
                    LiftCard(
                        modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                        lift = lift,
                        onClick = {
                            showSelector = false
                            liftSelected(it)
                        }
                    )
                }
            }
        }
    }

    when (lift) {
        null -> {
            Card(
                modifier = Modifier.width(96.dp),
                onClick = { showSelector = true }
            ) {
                Text(
                    text = "Select Lift",
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }

        else -> {
            LiftCard(
                modifier = Modifier.width(96.dp),
                lift = lift,
                onClick = { showSelector = true }
            )
        }
    }
}