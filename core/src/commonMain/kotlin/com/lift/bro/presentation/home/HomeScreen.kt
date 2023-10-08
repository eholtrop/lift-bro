package com.lift.bro.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton
import comliftbrodb.Lift
import spacing


@Composable
fun HomeScreen(
    database: LBDatabase = dependencies.database,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
) {
    val state by database.liftDataSource.getAll().collectAsState(null)

    when {
        state?.isEmpty() == true -> EmptyHomeScreen(addLiftClicked)
        state?.isNotEmpty() == true -> LiftListScreen(
            lifts = state!!,
            addLiftClicked = addLiftClicked,
            liftClicked = liftClicked,
            addSetClicked = addSetClicked,
        )
    }
}

@Composable
fun LiftListScreen(
    lifts: List<Lift>,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
) {

    LiftingScaffold(
        fabText = "Add Set",
        fabClicked = addSetClicked,
        topBar = {
            TopBar(
                title = "Lift Bro",
                trailingContent = {
                    TopBarIconButton(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Lift",
                        onClick = addLiftClicked,
                    )
                }
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Adaptive(96.dp),
            contentPadding = PaddingValues(MaterialTheme.spacing.one)
        ) {
            items(lifts) { lift ->
                LiftCard(
                    modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                    lift = lift,
                    onClick = liftClicked
                )
            }
        }
    }
}

@Composable
fun EmptyHomeScreen(
    addLiftClicked: () -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Do you even Lift Bro?",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
        Button(
            onClick = addLiftClicked
        ) {
            Text("Add Lift")
        }
    }
}