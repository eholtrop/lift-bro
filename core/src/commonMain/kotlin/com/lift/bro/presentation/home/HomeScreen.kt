package com.lift.bro.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import comliftbrodb.Lift
import spacing


@Composable
fun HomeScreen(
    database: LBDatabase = dependencies.database,
    addLiftClicked: () -> Unit,
) {
    val state by database.liftDataSource.getAll().collectAsState(emptyList())

    when {
        state.isEmpty() -> EmptyHomeScreen(addLiftClicked)

        else -> LiftListScreen(
            lifts = state,
            addLiftClicked = addLiftClicked
        )
    }
}

@Composable
fun LiftListScreen(
    lifts: List<Lift>,
    addLiftClicked: () -> Unit,
) {

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(96.dp),
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {
            items(lifts) { lift ->
                Box(
                    modifier = Modifier
                        .padding(MaterialTheme.spacing.quarter)
                        .aspectRatio(1f)
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .padding(MaterialTheme.spacing.quarter),
                    contentAlignment = Alignment.Center
                ) {
                    if (lift.name?.isNotEmpty() == true) {
                        Text(
                            text = lift.name,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = addLiftClicked
        ) {
            Text("Add Lift")
        }
    }
}

@Composable
fun EmptyHomeScreen(
    addLiftClicked: () -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            modifier = Modifier.align(Alignment.Center),
            onClick = addLiftClicked
        ) {
            Text("Add Lift")
        }
    }
}