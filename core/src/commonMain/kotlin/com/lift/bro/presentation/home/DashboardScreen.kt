@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lift.bro.data.BackupRestore
import com.lift.bro.data.LBDatabase
import com.lift.bro.defaultSbdLifts
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing
import com.lift.bro.ui.Images
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBarIconButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


@Composable
fun DashboardScreen(
    database: LBDatabase = dependencies.database,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val state by database.liftDataSource.getAll().collectAsStateWithLifecycle(null)
    val sets by dependencies.database.setDataSource.listenAll().collectAsStateWithLifecycle(emptyList())
    val variations by dependencies.database.variantDataSource.listenAll().collectAsStateWithLifecycle(emptyList())

    when {
        state?.isEmpty() == true -> EmptyHomeScreen(addLiftClicked)
        state?.isNotEmpty() == true -> DashboardContent(
            lifts = state!!,
            sets = sets,
            variations = variations,
            addLiftClicked = addLiftClicked,
            liftClicked = liftClicked,
            addSetClicked = addSetClicked,
            setClicked = setClicked,
        )
    }
}

private enum class Tab {
    Lifts,
    RecentSets,
}

@Composable
fun DashboardContent(
    lifts: List<Lift>,
    sets: List<LBSet>,
    variations: List<Variation>,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {

    var tab by rememberSaveable { mutableStateOf(Tab.Lifts) }

    LiftingScaffold(
        title = "Lift Bro",
        showBackButton = false,
        actions = {
            TopBarIconButton(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Lift",
                onClick = addLiftClicked,
            )
        },
        fabIcon = Icons.Default.Add,
        contentDescription = "Add Set",
        fabClicked = addSetClicked,
        preFab = {
            Button(
                modifier = Modifier.size(72.dp, 52.dp),
                onClick = {
                    tab = Tab.Lifts
                },
                shape = RoundedCornerShape(
                    topStartPercent = 50,
                    bottomStartPercent = 50,
                    topEndPercent = 25,
                    bottomEndPercent = 25,
                )
            ) {
                Column {
                    Icon(
                        painter = Images.dashboardMenuIcon(),
                        contentDescription = "Dashboard"
                    )

                    AnimatedVisibility(tab == Tab.Lifts) {
                        Box(
                            modifier = Modifier.padding(top = MaterialTheme.spacing.quarter.div(2))
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                        )
                    }
                }
            }
        },
        postFab = {
            Button(
                modifier = Modifier.size(72.dp, 52.dp),
                onClick = {
                    tab = Tab.RecentSets
                },
                shape = RoundedCornerShape(
                    topStartPercent = 25,
                    bottomStartPercent = 25,
                    topEndPercent = 50,
                    bottomEndPercent = 50,
                )
            ) {
                Column(
                ) {
                    Icon(
                        painter = Images.calendarMenuIcon(),
                        contentDescription = "Calendar"
                    )

                    AnimatedVisibility(tab == Tab.RecentSets) {
                        Box(
                            modifier = Modifier.padding(top = MaterialTheme.spacing.quarter.div(2))
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                        )
                    }
                }
            }
        },
    ) { padding ->
        when (tab) {
            Tab.Lifts -> {
                LazyVerticalGrid(
                    modifier = Modifier.padding(padding),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(MaterialTheme.spacing.one),
                ) {

                    items(lifts) { lift ->
                        LiftCard(
                            modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                            lift = lift,
                            onClick = liftClicked
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }

            Tab.RecentSets -> {
                CalendarScreen(
                    modifier = Modifier.padding(padding),
                    setClicked = setClicked,
                    sets = sets,
                    variations = variations
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
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

        val coroutineScope = rememberCoroutineScope()
        Button(
            onClick = {
                coroutineScope.launch {
                    BackupRestore.restore(defaultSbdLifts).first()
                }
            }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Add Default Lifts/Variations",
                )
                Text(
                    text = "Squat, Bench, Deadlift (SBD)",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}