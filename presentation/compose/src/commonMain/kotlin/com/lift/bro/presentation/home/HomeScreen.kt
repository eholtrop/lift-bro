package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.lift.bro.config.BuildConfig
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.dashboard.DashboardContent
import com.lift.bro.presentation.workout.WorkoutCalendarContent
import com.lift.bro.presentation.wrapped.WrappedDialog
import com.lift.bro.ui.AnimatedRotatingText
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.calendar.today
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.coroutines.delay
import kotlinx.datetime.Month
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
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import org.jetbrains.compose.ui.tooling.preview.PreviewParameterProvider
import tv.dpal.navi.LocalNavCoordinator

@Composable
fun HomeScreen(
    interactor: HomeInteractor = rememberHomeInteractor(),
) {
    val state by interactor.state.collectAsState()

    HomeScreenContent(state) {
        interactor(it)
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun HomeScreenContent(
    state: HomeState,
    onEvent: (HomeEvent) -> Unit,
) {
    when (val currentState = state) {
        is HomeState.Content -> {
            LiftingScaffold(
                description = {
                    AnimatedRotatingText(
                        modifier = Modifier
                            .clip(
                                MaterialTheme.shapes.small,
                            ).clickable(
                                onClick = { onEvent(HomeEvent.GoalsClicked) },
                                role = Role.Button
                            )
                            .padding(horizontal = MaterialTheme.spacing.half),
                        text = currentState.goals,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                    )
                },
                title = {
                    Column {
                        var showWrapped by remember { mutableStateOf(false) }

                        if (showWrapped) {
                            WrappedDialog(
                                year = today.year - 1,
                                onDismissRequest = { showWrapped = false }
                            )
                        }
                        Space(MaterialTheme.spacing.half)
                        Row(
                            modifier = Modifier.animateContentSize(),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.size(52.dp),
                                painter = painterResource(LocalLiftBro.current.iconRes()),
                                contentDescription = ""
                            )
                            Text(stringResource(Res.string.dashboard_title))

                            if (today.month == Month.JANUARY || BuildConfig.isDebug) {
                                var visible by rememberSaveable { mutableStateOf(false) }

                                Space(MaterialTheme.spacing.half)

                                AnimatedVisibility(
                                    visible = visible,
                                    enter = fadeIn(
                                        animationSpec = tween(
                                            delayMillis = 500,
                                            durationMillis = 500,
                                            easing = LinearOutSlowInEasing
                                        )
                                    ),
                                    exit = fadeOut()
                                ) {
                                    Button(
                                        colors = ButtonDefaults.textButtonColors(),
                                        contentPadding = PaddingValues(
                                            horizontal = MaterialTheme.spacing.one,
                                            vertical = MaterialTheme.spacing.quarter
                                        ),
                                        onClick = {
                                            showWrapped = true
                                        },
                                    ) {
                                        Column {
                                            Text("\uD83C\uDF89 \uD83C\uDF81")
                                        }
                                    }
                                }
                                LaunchedEffect(Unit) {
                                    delay(1000L)
                                    visible = true
                                }
                            }
                        }
                    }
                },
                leadingContent = {
                },
                trailingContent = {
                    TopBarIconButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            Res.string.dashboard_toolbar_leading_button_content_description
                        )
                    ) {
                        onEvent(HomeEvent.SettingsClicked)
                    }
                },
                fabProperties = FabProperties(
                    fabIcon = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.dashboard_fab_content_description),
                    fabClicked = { onEvent(HomeEvent.AddSetClicked) },
                    preFab = {
                        Button(
                            modifier = Modifier.size(72.dp, 52.dp),
                            onClick = {
                                onEvent(HomeEvent.DashboardClicked)
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
                                    contentDescription = stringResource(
                                        Res.string.dashboard_footer_leading_button_content_description
                                    ),
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
                                onEvent(HomeEvent.CalendarClicked)
                            },
                            shape = RoundedCornerShape(
                                topStartPercent = 25,
                                bottomStartPercent = 25,
                                topEndPercent = 50,
                                bottomEndPercent = 50,
                            )
                        ) {
                            Column {
                                Icon(
                                    painter = painterResource(Res.drawable.ic_calendar),
                                    contentDescription = stringResource(
                                        Res.string.dashboard_footer_trailing_button_content_description
                                    ),
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
                onEvent(HomeEvent.AddLiftClicked)
            },
            loadDefaultLifts = {}
        )

        HomeState.Loading -> {
            val navCoordinator = LocalNavCoordinator.current
            LiftingScaffold(
                title = {
                    Text("Loading...")
                },
                trailingContent = {
                    TopBarIconButton(
                        imageVector = Icons.Default.Settings,
                        contentDescription = stringResource(
                            Res.string.dashboard_toolbar_leading_button_content_description
                        )
                    ) {
                        navCoordinator.present(Destination.Settings)
                    }
                },
                leadingContent = {
                },
            ) { padding ->
                Column(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

class HomeStateProvider : PreviewParameterProvider<HomeState> {
    override val values: Sequence<HomeState>
        get() = sequenceOf(
            // Loading state
            HomeState.Loading,
            // Empty state (no lifts)
            HomeState.Empty,
            // Content with Dashboard selected, no goals
            HomeState.Content(
                selectedTab = Tab.Dashboard,
                goals = emptyList()
            ),
            // Content with Dashboard selected, with goals
            HomeState.Content(
                selectedTab = Tab.Dashboard,
                goals = listOf(
                    "Squat 405 lbs",
                    "Bench 315 lbs",
                    "Deadlift 500 lbs"
                )
            ),
            // Content with Calendar selected
            HomeState.Content(
                selectedTab = Tab.WorkoutCalendar,
                goals = listOf(
                    "Consistency: 4x per week",
                    "Linear progression on main lifts"
                )
            )
        )
}

@Preview
@Composable
fun HomeScreenContentPreview(
    @PreviewParameter(HomeStateProvider::class) state: HomeState
) {
    PreviewAppTheme(isDarkMode = false) {
        HomeScreenContent(
            state = state,
            onEvent = {}
        )
    }
}
