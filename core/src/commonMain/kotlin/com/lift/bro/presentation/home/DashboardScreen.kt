@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.LocalLiftCardYValue
import com.lift.bro.presentation.LocalUnitOfMeasure
import com.lift.bro.presentation.ads.AdBanner
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftCardState
import com.lift.bro.ui.LiftCardYValue
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.theme.spacing
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_fab_content_description
import lift_bro.core.generated.resources.dashboard_footer_leading_button_content_description
import lift_bro.core.generated.resources.dashboard_footer_trailing_button_content_description
import lift_bro.core.generated.resources.dashboard_footer_version
import lift_bro.core.generated.resources.dashboard_title
import lift_bro.core.generated.resources.dashboard_toolbar_leading_button_content_description
import lift_bro.core.generated.resources.ic_calendar
import lift_bro.core.generated.resources.reps
import lift_bro.core.generated.resources.view_dashboard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

data class DashboardState(
    val showEmpty: Boolean,
    val items: List<DashboardListItem>,
    val workouts: List<Workout>,
    val logs: List<LiftingLog>,
)

sealed class DashboardListItem {
    data class LiftCard(val state: LiftCardState) : DashboardListItem()
    object Ad : DashboardListItem()
}

sealed class DashboardEvent {
    object RestoreDefaultLifts : DashboardEvent()
}

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (Variation, LocalDate) -> Unit,
) {

    val state by remember { viewModel }.state.collectAsState(null)

    state?.let {
        Crossfade(it.showEmpty) { showEmpty ->
            when (showEmpty) {
                true -> EmptyHomeScreen(
                    addLiftClicked = addLiftClicked,
                    loadDefaultLifts = {
                        viewModel.handleEvent(DashboardEvent.RestoreDefaultLifts)
                    }
                )

                false -> {
                    DashboardContent(
                        dashboardState = it,
                        addLiftClicked = addLiftClicked,
                        liftClicked = liftClicked,
                        addSetClicked = addSetClicked,
                        setClicked = setClicked,
                    )
                }
            }
        }
    }
}

enum class Tab {
    Lifts,
    RecentSets,
}

@Composable
fun DashboardContent(
    dashboardState: DashboardState,
    defaultTab: Tab = Tab.Lifts,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (Variation, LocalDate) -> Unit,
    navCoordinator: NavCoordinator = LocalNavCoordinator.current,
) {
    var tab by rememberSaveable { mutableStateOf(defaultTab) }

    LiftingScaffold(
        title = {
            Icon(
                modifier = Modifier.size(52.dp),
                painter = painterResource(LocalLiftBro.current.iconRes()),
                contentDescription = ""
            )
            Text(stringResource(Res.string.dashboard_title))
        },
        leadingContent = {
        },
        trailingContent = {
            TopBarIconButton(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(Res.string.dashboard_toolbar_leading_button_content_description)
            ) {
                navCoordinator.present(Destination.Settings)
            }
        },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.dashboard_fab_content_description),
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
                            painter = painterResource(Res.drawable.view_dashboard),
                            contentDescription = stringResource(Res.string.dashboard_footer_leading_button_content_description),
                        )

                        AnimatedVisibility(tab == Tab.Lifts) {
                            Box(
                                modifier = Modifier.padding(
                                    top = MaterialTheme.spacing.quarter.div(
                                        2
                                    )
                                )
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
                            painter = painterResource(Res.drawable.ic_calendar),
                            contentDescription = stringResource(Res.string.dashboard_footer_trailing_button_content_description),
                        )

                        AnimatedVisibility(tab == Tab.RecentSets) {
                            Box(
                                modifier = Modifier.padding(
                                    top = MaterialTheme.spacing.quarter.div(
                                        2
                                    )
                                )
                                    .height(2.dp)
                                    .fillMaxWidth()
                                    .background(
                                        color = MaterialTheme.colorScheme.onPrimary,
                                    )
                            )
                        }
                    }
                }
            }
        ),
    ) { padding ->
        when (tab) {
            Tab.Lifts -> {
                val showWeight = LocalLiftCardYValue.current
                LazyVerticalGrid(
                    modifier = Modifier.padding(padding),
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(MaterialTheme.spacing.one),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half),
                ) {
                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Button(
                                onClick = {
                                    showWeight.value =
                                        if (showWeight.value == LiftCardYValue.Weight) LiftCardYValue.Reps else LiftCardYValue.Weight
                                }
                            ) {
                                Text(
                                    text = if (showWeight.value == LiftCardYValue.Weight) LocalUnitOfMeasure.current.value else stringResource(
                                        Res.string.reps
                                    )
                                )
                            }
                        }
                    }

                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        ReleaseNotesRow(
                            modifier = Modifier.height(72.dp)
                        )
                    }

                    items(
                        dashboardState.items,
                        span = { item ->
                            if (item is DashboardListItem.Ad) GridItemSpan(2) else GridItemSpan(1)
                        }) { s ->
                        when (val state = s) {
                            DashboardListItem.Ad -> {
                                AdBanner(
                                    modifier = Modifier.defaultMinSize(
                                        minHeight = 52.dp
                                    )
                                )
                            }

                            is DashboardListItem.LiftCard -> {
                                LiftCard(
                                    state = state.state,
                                    onClick = liftClicked,
                                    value = showWeight.value
                                )
                            }
                        }
                    }

                    val addButtonGridSize =
                        if (dashboardState.items.size % 2 == 0) GridItemSpan(1) else GridItemSpan(2)
                    item(
                        span = { addButtonGridSize }
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                                .then(
                                    if (addButtonGridSize.currentLineSpan == 1) Modifier.aspectRatio(
                                        1f
                                    ) else Modifier
                                )
                        ) {
                            Button(
                                modifier = Modifier.align(Alignment.Center),
                                onClick = addLiftClicked,
                                colors = ButtonDefaults.outlinedButtonColors()
                            ) {
                                Text("Add Lift")
                            }
                        }
                    }

                    item(
                        span = { GridItemSpan(2) }
                    ) {
                        Text(
                            stringResource(
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

            Tab.RecentSets -> {
                WorkoutCalendarScreen(
                    modifier = Modifier.padding(padding),
                    variationClicked = setClicked,
                    workouts = dashboardState.workouts,
                    logs = dashboardState.logs,
                )
            }
        }
    }
}