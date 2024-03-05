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
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.components.Calendar
import com.lift.bro.presentation.components.today
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedMax
import com.lift.bro.presentation.variation.formattedReps
import com.lift.bro.presentation.variation.formattedTempo
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

@Composable
fun LiftListScreen(
    lifts: List<Lift>,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
) {

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
    ) { padding ->

        LazyVerticalGrid(
            modifier = Modifier.padding(padding),
            columns = GridCells.Adaptive(96.dp),
            contentPadding = PaddingValues(MaterialTheme.spacing.one),
        ) {

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    modifier = Modifier.semantics { heading() },
                    text = "Lifts",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(lifts) { lift ->
                LiftCard(
                    modifier = Modifier.padding(MaterialTheme.spacing.quarter),
                    lift = lift,
                    onClick = liftClicked
                )
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
            ) {
                Text(
                    modifier = Modifier.semantics { heading() }
                        .padding(top = MaterialTheme.spacing.one),
                    text = "Recent Sets",
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            item(
                span = { GridItemSpan(maxLineSpan) }
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

                    val navigator = LocalNavigator.current

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        items(sets.filter { it.date.toLocalDate() == selectedDate }) { set ->
                            Card {
                                Row(
                                    modifier = Modifier.clickable(
                                        onClick = { setClicked(set) }
                                    ).padding(MaterialTheme.spacing.half)
                                ) {

                                    val variation =
                                        dependencies.database.variantDataSource.get(set.variationId)
                                    val lift by dependencies.database.liftDataSource.get(variation?.liftId)
                                        .collectAsState(null)

                                    Column {
                                        Text(
                                            text = "${variation?.name} ${lift?.name}",
                                            style = MaterialTheme.typography.bodyMedium,
                                        )
                                        Text(
                                            text = "${set.reps} x ${set.formattedWeight}",
                                            style = MaterialTheme.typography.labelMedium,
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            IconButton(
                                onClick = addSetClicked
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add Set"
                                )
                            }
                        }
                    }

                    Space(MaterialTheme.spacing.one)
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
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