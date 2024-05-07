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
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.set.LineItem
import com.lift.bro.presentation.spacing

@Composable
fun LiftSelector(
    modifier: Modifier = Modifier,
    lift: Lift?,
    liftSelected: (Lift) -> Unit,
) {
    var showSelector by remember { mutableStateOf(false) }

    if (showSelector) {
        LiftSelectorDialog(
            onDismissRequest = {
                showSelector = false
            },
            liftSelected = {
                liftSelected(it)
                showSelector = false
            }
        )
    }

    LineItem(
        title = "Lift",
        description = lift?.name ?: "Select Lift",
        onClick = { showSelector = true }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LiftSelectorDialog(
    onDismissRequest: () -> Unit,
    liftSelected: (Lift) -> Unit,
) {
    AlertDialog(
        modifier = Modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
        ),
        onDismissRequest = onDismissRequest,
    ) {
        val lifts by dependencies.database.liftDataSource.getAll().collectAsState(emptyList())

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {
            items(lifts) { lift ->
                LiftCard(
                    modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                    lift = lift,
                    onClick = liftSelected
                )
            }
        }
    }
}