@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.dashboard

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.unit.dp
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.dashboard.DashboardEvent.LiftClicked
import com.lift.bro.presentation.workout.WorkoutCalendarContent
import com.lift.bro.ui.Card
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.card.lift.LiftCard
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_footer_version
import org.jetbrains.compose.resources.stringResource
import tv.dpal.compose.padding.horizontal.padding

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
                                DashboardLiftHeader(
                                    v2 = item.v3,
                                    showRpe = showRpe,
                                    title = "Categories",
                                    showTempo = showTempo,
                                    sortingSettings = state.sortingSettings,
                                    onToggleRpe = { showRpe = !showRpe },
                                    onToggleTempo = { showTempo = !showTempo },
                                    toggleFavourite = { interactor(DashboardEvent.FavouritesAtTopToggled) },
                                    optionSelected = { interactor(DashboardEvent.SortingOptionSelected(it)) },
                                    onAddClicked = { interactor(DashboardEvent.AddLiftClicked) }
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

private fun DashboardListItem.gridSize(listSize: Int = 0): Int = when (this) {
    is DashboardListItem.LiftCard -> 1
    is DashboardListItem.LiftHeader -> 2
    DashboardListItem.ReleaseNotes -> 2
    DashboardListItem.WorkoutCalendar -> 2
}
