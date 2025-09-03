package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.dashboard.DashboardContent
import com.lift.bro.presentation.workout.WorkoutCalendarContent
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_fab_content_description
import lift_bro.core.generated.resources.dashboard_footer_leading_button_content_description
import lift_bro.core.generated.resources.dashboard_footer_trailing_button_content_description
import lift_bro.core.generated.resources.dashboard_title
import lift_bro.core.generated.resources.dashboard_toolbar_leading_button_content_description
import lift_bro.core.generated.resources.ic_calendar
import lift_bro.core.generated.resources.view_dashboard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreen(
    interactor: Interactor<HomeState, HomeEvent> = rememberHomeInteractor(),
) {
    val state by interactor.state.collectAsState()

    when (val currentState = state) {
        is HomeState.Content -> {
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
                        interactor(HomeEvent.SettingsClicked)
                    }
                },
                fabProperties = FabProperties(
                    fabIcon = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.dashboard_fab_content_description),
                    fabClicked = { interactor(HomeEvent.AddSetClicked) },
                    preFab = {
                        Button(
                            modifier = Modifier.size(72.dp, 52.dp),
                            onClick = {
                                interactor(HomeEvent.DashboardClicked)
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

                                AnimatedVisibility(currentState.selectedTab == Tab.Dashboard) {
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
                                interactor(HomeEvent.CalendarClicked)
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

                                AnimatedVisibility(currentState.selectedTab == Tab.WorkoutCalendar) {
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
                Crossfade(
                    currentState.selectedTab
                ) { tab ->
                    when (tab) {
                        Tab.Dashboard -> {
                            DashboardContent(
                                modifier = Modifier.padding(padding),
                            )
                        }

                        Tab.WorkoutCalendar -> {
                            WorkoutCalendarContent(
                                modifier = Modifier.padding(padding),
                            )
                        }
                    }
                }
            }
        }

        HomeState.Empty -> EmptyHomeScreen(
            addLiftClicked = {
                interactor(HomeEvent.AddLiftClicked)
            },
            loadDefaultLifts = {}
        )
        HomeState.Loading -> {}
    }
}