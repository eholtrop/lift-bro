@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.dashboard.DashboardEvent.*
import com.lift.bro.presentation.dashboard.DashboardEvent.LiftClicked
import com.lift.bro.presentation.set.ChipButton
import com.lift.bro.presentation.workout.WorkoutCalendarContent
import com.lift.bro.ui.Card
import com.lift.bro.ui.CheckField
import com.lift.bro.ui.RadioField
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.Space
import com.lift.bro.ui.card.lift.DashboardGraphCard
import com.lift.bro.ui.card.lift.LiftCard
import com.lift.bro.ui.card.lift.LiftCardYValue
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.reps
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.horizontal.padding
import tv.dpal.compose.padding.vertical.padding

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    interactor: DashboardInteractor,
) {
    val state by interactor.state.collectAsState()

    val showWeight = LocalLiftCardYValue.current

    Crossfade(
        modifier = modifier,
        targetState = state,
        label = "DashboardContent"
    ) { state ->
        when (state) {
            is Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is Loaded -> {
                var showRpe by rememberSaveable { mutableStateOf(true) }
                var showTempo by rememberSaveable { mutableStateOf(true) }

                val numOddItems by remember { mutableStateOf(state.items.sumOf { it.gridSize(state.items.size) % 2 }) }
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(
                        horizontal = MaterialTheme.spacing.half,
                        vertical = MaterialTheme.spacing.half
                    ),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                ) {
                    itemsIndexed(
                        state.items,
                        span = { index, item -> GridItemSpan(item.gridSize(state.items.size)) }
                    ) { index, item ->
                        when (item) {
                            is DashboardListItem.LiftHeader -> {
                                LiftHeader(
                                    v2 = item.v3,
                                    showRpe = showRpe,
                                    showTempo = showTempo,
                                    sortingSettings = state.sortingSettings,
                                    onToggleRpe = { showRpe = !showRpe },
                                    onToggleTempo = { showTempo = !showTempo },
                                    toggleFavourite = { interactor(FavouritesAtTopToggled) },
                                    optionSelected = { interactor(SortingOptionSelected(it)) }
                                )
                            }

                            is DashboardListItem.LiftCard -> {
                                when (val card = item as DashboardListItem.LiftCard) {
                                    is DashboardListItem.LiftCard.Loaded -> {
                                        LiftCard(
                                            modifier = Modifier.padding(
                                                start = when {
                                                    (index % 2) != (numOddItems % 2) -> MaterialTheme.spacing.half
                                                    else -> 0.dp
                                                },
                                                end = when {
                                                    (index % 2) == (numOddItems % 2) -> MaterialTheme.spacing.half
                                                    else -> 0.dp
                                                },
                                            ),
                                            showRpe = showRpe,
                                            showTempo = showTempo,
                                            state = card.state,
                                            onClick = { interactor(LiftClicked(card.state.lift.id)) },
                                            yUnit = showWeight.value
                                        )
                                    }

                                    DashboardListItem.LiftCard.Loading -> {
                                        Card(
                                            modifier = Modifier
                                                .aspectRatio(1f),
                                        ) {
                                        }
                                    }
                                }
                            }

                            DashboardListItem.WorkoutCalendar -> {
                                WorkoutCalendarContent(
                                    modifier = Modifier
                                        .padding(horizontal = MaterialTheme.spacing.half)
                                )
                            }

                            DashboardListItem.ReleaseNotes -> {
                                ReleaseNotesRow(
                                    modifier = Modifier.height(72.dp)
                                        .padding(horizontal = MaterialTheme.spacing.half)
                                )
                            }

                            DashboardListItem.AddLiftButton -> {
                                Box(

                                    modifier = Modifier.fillMaxSize()
                                        .then(
                                            if (item.gridSize(state.items.size) == 1) {
                                                Modifier.aspectRatio(
                                                    1f
                                                )
                                            } else {
                                                Modifier
                                            }
                                        )
                                ) {
                                    Button(
                                        modifier = Modifier.align(Alignment.Center),
                                        onClick = {
                                            interactor(AddLiftClicked)
                                        },
                                        colors = ButtonDefaults.elevatedButtonColors()
                                    ) {
                                        Text("Add Lift")
                                    }
                                }
                            }

                            is DashboardListItem.GraphCard -> DashboardGraphCard(
                                state = item,
                                onClick = {},
                                showRpe = false,
                                showTempo = false,
                                yUnit = LiftCardYValue.Weight
                            )

                            is DashboardListItem.NoGraphsMigration -> {
                                Column {
                                    Text(
                                        text = "Lifts are gone! \uD83D\uDE31",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                    Text(
                                        text = "Your Coach has replaced them with \"Filters\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = "Check out some of the filter templates to see even cooler stats then before!!",
                                        style = MaterialTheme.typography.titleSmall
                                    )

                                    Row {
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "One Rep Max",
                                            description = "Any sets with a rep of 1",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "SBD",
                                            description = "All Sqat, Bench, and Deadlift",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                    }
                                    Row {
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "Only Favs",
                                            description = "Anything marked as favourite",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "Bodyweight",
                                            description = "All variations flagged as bodyweight",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                    }
                                    Text(
                                        "Dont worry, you can still track your favourites!",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    Row {
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "Squats",
                                            description = "All variations under the Lift \"Squat\"",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                        CheckField(
                                            modifier = Modifier.weight(1f),
                                            title = "Deadlifts",
                                            description = "All variations under the Lift \"Squat\"",
                                            checked = false,
                                            checkChanged = {}
                                        )
                                    }
                                }
                            }

                        }
                    }

                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(horizontal = MaterialTheme.spacing.half),
                            text = stringResource(
                                Res.string.dashboard_footer_version,
                                BuildKonfig.VERSION_NAME
                            )
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LiftHeader(
    modifier: Modifier = Modifier,
    v2: Boolean,
    sortingSettings: SortingSettings,
    showWeight: MutableState<LiftCardYValue> = LocalLiftCardYValue.current,
    showTempo: Boolean,
    showRpe: Boolean,
    onToggleTempo: () -> Unit,
    onToggleRpe: () -> Unit,
    optionSelected: (SortingOption) -> Unit,
    toggleFavourite: () -> Unit,
) {
    when (v2) {
        false -> Row(
            modifier = modifier
                .padding(top = MaterialTheme.spacing.two),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.medium
                ).border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    shape = MaterialTheme.shapes.medium
                )
                    .animateContentSize()
            ) {
                var showButtons by remember { mutableStateOf(false) }
                if (!showButtons) {
                    IconButton(
                        onClick = {
                            showButtons = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Graph Settings"
                        )
                    }
                } else {
                    Button(
                        colors = ButtonDefaults.textButtonColors(),
                        shape = MaterialTheme.shapes.medium.copy(
                            topEnd = CornerSize(0.dp),
                            bottomEnd = CornerSize(0.dp),
                        ),
                        onClick = {
                            showWeight.value =
                                if (showWeight.value == LiftCardYValue.Weight) {
                                    LiftCardYValue.Reps
                                } else {
                                    LiftCardYValue.Weight
                                }
                        }
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = LocalTextStyle.current
                                        .copy(
                                            color = if (showWeight.value == LiftCardYValue.Weight) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                TextFieldDefaults.colors().disabledTextColor
                                            }
                                        )
                                        .toSpanStyle()
                                ) {
                                    append(LocalUnitOfMeasure.current.value)
                                }
                                append("/")
                                withStyle(
                                    style = LocalTextStyle.current
                                        .copy(
                                            color = if (showWeight.value == LiftCardYValue.Reps) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                TextFieldDefaults.colors().disabledTextColor
                                            }
                                        )
                                        .toSpanStyle()
                                ) {
                                    append(stringResource(Res.string.reps))
                                }
                            }
                        )
                    }
                    Button(
                        colors = ButtonDefaults.textButtonColors(),
                        shape = RectangleShape,
                        onClick = {
                            onToggleRpe()
                        }
                    ) {
                        Text(
                            text = "rpe",
                            color = if (showRpe) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                TextFieldDefaults.colors().disabledTextColor
                            }
                        )
                    }
                    Button(
                        colors = ButtonDefaults.textButtonColors(),
                        shape = RectangleShape,
                        onClick = {
                            onToggleTempo()
                        }
                    ) {
                        Text(
                            text = "tempo",
                            color = if (showTempo) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                TextFieldDefaults.colors().disabledTextColor
                            }
                        )
                    }
                }
            }
        }

        true -> {
            Column(
                modifier = Modifier
                    .padding(
                        top = MaterialTheme.spacing.one,
                        bottom = MaterialTheme.spacing.half
                    )
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surface,
                                Color.Transparent,
                            ),
                            end = Offset(80f, 50f),
                        ),
                        shape = MaterialTheme.shapes.medium.copy(
                            bottomEnd = CornerSize(0.dp),
                            bottomStart = CornerSize(0.dp),
                        ),
                    ).padding(
                        top = MaterialTheme.spacing.threeQuarters,
                        horizontal = MaterialTheme.spacing.half
                    ),
            ) {
                Text(
                    text = "Lift Stats \uD83E\uDD13",
                    style = MaterialTheme.typography.titleLarge
                )
                Space(MaterialTheme.spacing.quarter)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                ) {
                    ChipButton(
                        onClick = {
                            showWeight.value =
                                if (showWeight.value == LiftCardYValue.Weight) {
                                    LiftCardYValue.Reps
                                } else {
                                    LiftCardYValue.Weight
                                }
                        }
                    ) {
                        Text(
                            text = buildAnnotatedString {
                                withStyle(
                                    style = LocalTextStyle.current
                                        .copy(
                                            color = if (showWeight.value == LiftCardYValue.Weight) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                TextFieldDefaults.colors().disabledTextColor
                                            }
                                        )
                                        .toSpanStyle()
                                ) {
                                    append(LocalUnitOfMeasure.current.value)
                                }
                                append("/")
                                withStyle(
                                    style = LocalTextStyle.current
                                        .copy(
                                            color = if (showWeight.value == LiftCardYValue.Reps) {
                                                MaterialTheme.colorScheme.onPrimary
                                            } else {
                                                TextFieldDefaults.colors().disabledTextColor
                                            }
                                        )
                                        .toSpanStyle()
                                ) {
                                    append(stringResource(Res.string.reps))
                                }
                            }
                        )
                    }
                    ChipButton(
                        onClick = {
                            onToggleRpe()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null
                        )
                        Text(
                            text = "rpe",
                            color = if (showRpe) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                TextFieldDefaults.colors().disabledTextColor
                            }
                        )
                    }
                    ChipButton(
                        onClick = {
                            onToggleTempo()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null
                        )
                        Text(
                            text = "tempo",
                            color = if (showTempo) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                TextFieldDefaults.colors().disabledTextColor
                            }
                        )
                    }

                    Space()

                    var showSortingDialog by remember { mutableStateOf(false) }

                    if (showSortingDialog) {
                        DashboardSortingDialog(
                            sortingSettings = sortingSettings,
                            toggleFavourite = toggleFavourite,
                            optionSelected = optionSelected,
                            onDismissRequest = { showSortingDialog = false },
                        )
                    }

                    ChipButton(
                        onClick = {
                            showSortingDialog = true
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.Sort,
                            contentDescription = "Sort"
                        )
                    }
                }
            }
        }
    }
}

private fun DashboardListItem.gridSize(listSize: Int = 0): Int = when (this) {
    is DashboardListItem.LiftCard -> 1
    is DashboardListItem.LiftHeader -> 2
    DashboardListItem.ReleaseNotes -> 2
    DashboardListItem.WorkoutCalendar -> 2
    DashboardListItem.AddLiftButton -> if (listSize % 2 == 0) 2 else 1
    is DashboardListItem.GraphCard -> 1
    is DashboardListItem.NoGraphsMigration -> 2
}
