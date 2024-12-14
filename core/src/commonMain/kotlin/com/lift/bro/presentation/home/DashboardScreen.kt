@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.home

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.fullName
import com.lift.bro.presentation.components.Calendar
import com.lift.bro.presentation.components.CalendarDialog
import com.lift.bro.presentation.components.today
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.spacing
import com.lift.bro.presentation.toString
import com.lift.bro.presentation.variation.formattedMax
import com.lift.bro.presentation.variation.formattedReps
import com.lift.bro.presentation.variation.formattedTempo
import com.lift.bro.presentation.variation.formattedWeight
import com.lift.bro.presentation.variation.render
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.utils.toFirstDateOfWeek
import com.lift.bro.utils.toLastDateOfWeek
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus


@Composable
fun DashboardScreen(
    database: LBDatabase = dependencies.database,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (LBSet) -> Unit,
    seeAllClicked: () -> Unit,
) {
    val state by database.liftDataSource.getAll().collectAsState(null)

    when {
        state?.isEmpty() == true -> EmptyHomeScreen(addLiftClicked)
        state?.isNotEmpty() == true -> {
            LiftingScaffold(
                fabIcon = Icons.Default.Add,
                contentDescription = "Add Set",
                title = "Do you even Lift Bro?",
                fabClicked = addSetClicked
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).scrollable(rememberScrollState(), Orientation.Vertical)
                ) {

                    var showCalendar by remember { mutableStateOf(false) }
                    var selectedDate by remember {
                        mutableStateOf(
                            Clock.System.now().toLocalDate()
                        )
                    }
                    var visibleWeek by remember { mutableStateOf(selectedDate) }

                    val firstDayOfWeek by remember {
                        derivedStateOf {
                            visibleWeek.toFirstDateOfWeek()
                        }
                    }
                    val lastDayOfWeek by remember {
                        derivedStateOf {
                            visibleWeek.toLastDateOfWeek()
                        }
                    }

                    val sets = dependencies.database.setDataSource.getAll()
                        .groupBy { it.date.toLocalDate() }.toList()
                        .sortedByDescending { it.first }

                    val variations = dependencies.database.variantDataSource.getAll()

                    if (showCalendar) {
                        CalendarDialog(
                            modifier = Modifier.fillMaxWidth()
                                .wrapContentHeight(),
                            selectedDate = selectedDate,
                            contentPadding = PaddingValues(0.dp),
                            dateSelected = {
                                selectedDate = it
                            },
                            onDismissRequest = { showCalendar = false },
                            date = { date ->
                                val selected = date == selectedDate
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .background(
                                            color = if (selected) {
                                                MaterialTheme.colorScheme.primary
                                            } else if (sets.any { it.first == date }) {
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
                                        for (i in 1..kotlin.math.min(sets.first { it.first == date }
                                            .second
                                            .groupBy { it.variationId }.size, 3)) {
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

                    Card(
                        modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one)
                            .animateContentSize(),
                    ) {
                        Column(
                            modifier = Modifier.padding(MaterialTheme.spacing.half)
                        ) {

                            Row(
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "Recent Sets",
                                        style = MaterialTheme.typography.headlineSmall,
                                    )
                                    Text(
                                        text = "Week of ${firstDayOfWeek.toString("MMM d, yyyy")}",
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                }
                                Space()
                                IconButton(
                                    onClick = { showCalendar = showCalendar.not() }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Show Calendar"
                                    )
                                }
                            }

                            Space(MaterialTheme.spacing.half)

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        visibleWeek = visibleWeek.minus(DatePeriod(days = 7))
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Previous Week")
                                }
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            MaterialTheme.spacing.half,
                                            alignment = Alignment.CenterHorizontally
                                        ),
                                    ) {
                                        for (i in 0..3) {
                                            val thisDate = firstDayOfWeek.plus(DatePeriod(days = i))
                                            CalendarDateButton(
                                                modifier = Modifier.size(52.dp),
                                                selected = thisDate == selectedDate,
                                                date = thisDate,
                                                title = thisDate.dayOfWeek.toString().substring(0, 1),
                                                blips = sets.firstOrNull { it.first == thisDate }?.second?.size
                                                    ?: 0,
                                                onClick = { selectedDate = thisDate },
                                            )
                                        }
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(
                                            MaterialTheme.spacing.half,
                                            alignment = Alignment.CenterHorizontally
                                        ),
                                    ) {
                                        for (i in 4..6) {
                                            val thisDate = firstDayOfWeek.plus(DatePeriod(days = i))
                                            CalendarDateButton(
                                                modifier = Modifier.size(52.dp),
                                                selected = thisDate == selectedDate,
                                                date = thisDate,
                                                title = thisDate.dayOfWeek.toString().substring(0, 1),
                                                blips = sets.firstOrNull { it.first == thisDate }?.second?.size
                                                    ?: 0,
                                                onClick = { selectedDate = thisDate },
                                            )
                                        }
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        visibleWeek = visibleWeek.plus(DatePeriod(days = 7))
                                    }
                                ) {
                                    Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next Week")
                                }
                            }

                            Space(MaterialTheme.spacing.half)


                            Column {
                                sets.filter { it.first in firstDayOfWeek..lastDayOfWeek }.forEach { pair ->
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (pair.first == selectedDate) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(MaterialTheme.spacing.half)
                                        ) {
                                            Text(pair.first.toString("EEEE, MMM d - yyyy"))

                                            pair.second.groupBy { it.variationId }.forEach { pair ->
                                                Text(text = variations.firstOrNull { it.id == pair.key }?.fullName ?: "")
                                                pair.value.forEach {
                                                    Text(
                                                        text = "${it.reps} x ${it.formattedWeight} - ${it.formattedTempo}",
                                                        style = MaterialTheme.typography.bodyMedium,
                                                    )
                                                    if (it.notes.isNotBlank()) {
                                                        Text(it.notes)
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Space(MaterialTheme.spacing.half)
                                }
                            }
                        }
                    }

                    LiftCarousel(
                        liftClicked = liftClicked,
                        seeAllClicked = seeAllClicked,
                    )
                }
            }
        }
    }
}

@Composable
fun CalendarDateButton(
    modifier: Modifier = Modifier,
    date: LocalDate,
    title: String = date.dayOfMonth.toString(),
    selected: Boolean,
    blips: Int,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .clip(CircleShape)
            .background(
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else if (blips > 0) {
                    MaterialTheme.colorScheme.surfaceVariant
                } else {
                    Color.Transparent
                },
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
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
            for (i in 1..kotlin.math.min(blips, 3)) {
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

@Composable
fun LiftCarousel(
    liftClicked: (Lift) -> Unit,
    seeAllClicked: () -> Unit,
) {
    val lifts by dependencies.database.liftDataSource.getAll().collectAsState(null)
    lifts?.let {
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = "Lifts",
                style = MaterialTheme.typography.headlineSmall,
            )
            Space()
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.one)
        ) {
            items(items = it.take(5)) { lift ->
                LiftCard(
                    modifier = Modifier
                        .size(width = 196.dp, height = 128.dp)
                        .padding(MaterialTheme.spacing.quarter),
                    lift = lift,
                    onClick = liftClicked
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.one),
            verticalAlignment = Alignment.Bottom,
        ) {
            Space()
            Button(onClick = seeAllClicked) {
                Text("See All")
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