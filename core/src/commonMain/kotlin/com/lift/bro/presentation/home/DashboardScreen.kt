package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.components.Calendar
import com.lift.bro.presentation.components.today
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.ui.Images
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBar
import com.lift.bro.ui.TopBarIconButton


@Composable
fun DashboardScreen(
    database: LBDatabase = dependencies.database,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {
    val state by database.liftDataSource.getAll().collectAsState(null)

    when {
        state?.isEmpty() == true -> EmptyHomeScreen(addLiftClicked)
        state?.isNotEmpty() == true -> LiftListScreen(
            lifts = state!!,
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
fun LiftListScreen(
    lifts: List<Lift>,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {

    var tab by rememberSaveable { mutableStateOf(Tab.Lifts) }

    LiftingScaffold(
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
                RecentSetsCalendar(
                    modifier = Modifier.padding(padding),
                    setClicked = setClicked
                )
            }
        }
    }
}

@Composable
fun RecentSetsCalendar(
    modifier: Modifier = Modifier,
    setClicked: (LBSet) -> Unit,
) {
    var selectedDate by remember { mutableStateOf(today) }

    val sets = dependencies.database.setDataSource.getAll()

    val selectedVariations = sets.filter { it.date.toLocalDate() == selectedDate }
        .groupBy { it.variationId }
        .toList()
        .sortedByDescending { it.second.maxOf { it.weight } }

    LazyVerticalStaggeredGrid(
        modifier = modifier,
        columns = StaggeredGridCells.Fixed(2),
        verticalItemSpacing = MaterialTheme.spacing.half,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
        contentPadding = PaddingValues(MaterialTheme.spacing.one)
    ) {

        item(
            span = StaggeredGridItemSpan.FullLine
        ) {
            Calendar(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                selectedDate = selectedDate,
                contentPadding = PaddingValues(0.dp),
                dateSelected = {
                    selectedDate = it
                },
                date = { date ->
                    val selected = date == selectedDate
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = if (selected) {
                                    MaterialTheme.colorScheme.primary
                                } else if (sets.any { it.date.toLocalDate() == date }) {
                                    MaterialTheme.colorScheme.surfaceVariant
                                } else {
                                    Color.Transparent
                                },
                                shape = CircleShape
                            )
                            .clickable {
                                selectedDate = date
                            },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Space(MaterialTheme.spacing.quarter)

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                        ) {
                            for (i in 1..kotlin.math.min(sets.filter { it.date.toLocalDate() == date }.groupBy { it.variationId }.size, 3)) {
                                Box(
                                    modifier = Modifier.background(
                                        color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        shape = CircleShape,
                                    ).size(4.dp)
                                )
                            }
                        }
                    }
                }
            )
        }

        item(
            span = StaggeredGridItemSpan.FullLine
        ) {
            Text(
                text = selectedDate.toString("EEEE, MMM d yyyy"),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        items(
            selectedVariations
        ) { pair ->
            Card {
                Column(
                    modifier = Modifier.padding(MaterialTheme.spacing.half)
                ) {
                    val variation =
                        dependencies.database.variantDataSource.get(pair.first)
                    val lift by dependencies.database.liftDataSource.get(
                        variation?.liftId
                    )
                        .collectAsState(null)

                    Text(
                        text = "${variation?.name} ${lift?.name}",
                        style = MaterialTheme.typography.titleLarge,
                    )

                    pair.second.sortedByDescending { it.weight }
                        .forEach { set ->
                            Box(
                                modifier = Modifier.fillMaxWidth()
                                    .defaultMinSize(minHeight = 44.dp)
                                    .clickable(
                                        role = Role.Button,
                                        onClick = { setClicked(set) }
                                    ),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Text(
                                    text = "${set.formattedWeight} x ${set.reps}",
                                    style = MaterialTheme.typography.bodyLarge,
                                )
                            }
                        }
                }
            }
        }

        item(
            span = StaggeredGridItemSpan.FullLine
        ) {
            Spacer(modifier = Modifier.height(72.dp))
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