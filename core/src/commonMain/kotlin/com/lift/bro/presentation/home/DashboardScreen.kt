package com.lift.bro.presentation.home

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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
        },
    ) { padding ->

        var tab by rememberSaveable { mutableStateOf(Tab.Lifts) }

        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Row(
                    modifier = Modifier.clickable(
                        role = Role.Switch,
                        onClick = {
                            tab = when (tab) {
                                Tab.Lifts -> Tab.RecentSets
                                Tab.RecentSets -> Tab.Lifts
                            }
                        }
                    ),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        modifier = Modifier.semantics { heading() },
                        text = "Lifts",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (tab == Tab.Lifts) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        modifier = Modifier.semantics { heading() },
                        text = "/",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        modifier = Modifier.semantics { heading() },
                        text = "Recent Sets",
                        style = MaterialTheme.typography.headlineLarge,
                        color = if (tab == Tab.RecentSets) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                    )
                }
            }

            when (tab) {
                Tab.Lifts -> {
                    items(lifts) { lift ->
                        LiftCard(
                            modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                            lift = lift,
                            onClick = liftClicked
                        )
                    }

                }

                Tab.RecentSets -> {
                    item(
                        span = { GridItemSpan(maxLineSpan) }
                    ) {
                        RecentSetsCalendar(
                            setClicked = setClicked,
                            addSetClicked = addSetClicked
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }
}

@Composable
fun RecentSetsCalendar(
    setClicked: (LBSet) -> Unit,
    addSetClicked: () -> Unit,
) {
    var selectedDate by remember { mutableStateOf(today) }

    val sets = dependencies.database.setDataSource.getAll()

    Column {
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
                Box(
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
                    contentAlignment = Alignment.Center
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
                }
            }
        )

        Text(
            text = selectedDate.toString("EEEE, MMM d yyyy"),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Space(MaterialTheme.spacing.quarter)

        Column(
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {

            sets.filter { it.date.toLocalDate() == selectedDate }
                .groupBy { it.variationId }
                .toList()
                .sortedByDescending { it.second.maxOf { it.weight } }
                .chunked(2)
                .forEach { data ->

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
                    ) {

                        data.forEach { pair ->
                            Card(
                                modifier = Modifier.weight(1f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(MaterialTheme.spacing.half)
                                ) {
                                    val variation = dependencies.database.variantDataSource.get(pair.first)
                                    val lift by dependencies.database.liftDataSource.get(variation?.liftId)
                                        .collectAsState(null)

                                    Text(
                                        text = "${variation?.name} ${lift?.name}",
                                        style = MaterialTheme.typography.titleLarge,
                                    )

                                    pair.second.sortedByDescending { it.weight }.forEach { set ->
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
                    }

                }
        }

        Space(MaterialTheme.spacing.one)
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