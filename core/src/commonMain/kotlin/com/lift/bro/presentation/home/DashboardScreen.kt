@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class)

package com.lift.bro.presentation.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import com.lift.bro.data.BackupRestore
import com.lift.bro.data.LiftDataSource
import com.lift.bro.defaultSbdLifts
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftCard
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_empty_primary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_secondary_cta_subtitle
import lift_bro.core.generated.resources.dashboard_empty_secondary_cta_title
import lift_bro.core.generated.resources.dashboard_empty_title
import lift_bro.core.generated.resources.dashboard_footer_leading_button_content_description
import lift_bro.core.generated.resources.dashboard_footer_trailing_button_content_description
import lift_bro.core.generated.resources.dashboard_settings_fab_content_description
import lift_bro.core.generated.resources.dashboard_title
import lift_bro.core.generated.resources.dashboard_toolbar_leading_button_content_description
import lift_bro.core.generated.resources.dashboard_toolbar_trailing_button_content_description
import lift_bro.core.generated.resources.ic_calendar
import lift_bro.core.generated.resources.view_dashboard
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberLifts(): StateFlow<List<Lift>> {
    val stateFlow = MutableStateFlow<List<Lift>>(emptyList())

    LaunchedEffect(Unit) {
        dependencies.database.liftDataSource.listenAll()
            .catch {
                it.printStackTrace()
            }.collectLatest {
                stateFlow.tryEmit(it)
            }
    }
    return stateFlow
}

data class DashboardState(
    val showEmpty: Boolean,
    val lifts: List<Lift>,
)

sealed class DashboardEvent {
    object RestoreDefaultLifts : DashboardEvent()
}

class DashboardViewModel(
    initialState: DashboardState? = null,
    liftRepository: LiftDataSource = dependencies.database.liftDataSource,
    scope: CoroutineScope = GlobalScope
) {
    val state = liftRepository.listenAll()
        .map { it.sortedBy { it.name.toLowerCase(Locale.current) } }
        .map { DashboardState(showEmpty = it.isEmpty(), it) }
        .stateIn(scope, SharingStarted.Eagerly, initialState)

    fun handleEvent(event: DashboardEvent) {
        when (event) {
            DashboardEvent.RestoreDefaultLifts -> GlobalScope.launch {
                BackupRestore.restore(defaultSbdLifts).flowOn(Dispatchers.IO).collect()
            }
        }
    }
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
                        lifts = it.lifts,
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

private enum class Tab {
    Lifts,
    RecentSets,
}

@Composable
fun DashboardContent(
    lifts: List<Lift>,
    addLiftClicked: () -> Unit,
    liftClicked: (Lift) -> Unit,
    addSetClicked: () -> Unit,
    setClicked: (Variation, LocalDate) -> Unit,
) {

    var tab by rememberSaveable { mutableStateOf(Tab.Lifts) }

    LiftingScaffold(
        title = stringResource(Res.string.dashboard_title),
        leadingContent = {
            val navCoordinator = LocalNavCoordinator.current
            TopBarIconButton(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(Res.string.dashboard_toolbar_leading_button_content_description)
            ) {
                navCoordinator.present(Destination.Settings)
            }
        },
        trailingContent = {
            TopBarIconButton(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(Res.string.dashboard_toolbar_trailing_button_content_description),
                onClick = addLiftClicked,
            )
        },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.Add,
            contentDescription = stringResource(Res.string.dashboard_settings_fab_content_description),
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

        val sets by dependencies.database.setDataSource.listenAll()
            .collectAsState(emptyList())
        val variations by dependencies.database.variantDataSource.listenAll()
            .collectAsState(emptyList())

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
                    variationClicked = setClicked,
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
    loadDefaultLifts: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = stringResource(Res.string.dashboard_empty_title),
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.headlineLarge
        )
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
        Button(
            onClick = addLiftClicked
        ) {
            Text(
                text = stringResource(Res.string.dashboard_empty_primary_cta_title),)
        }
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))

        Button(
            onClick = loadDefaultLifts
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(Res.string.dashboard_empty_secondary_cta_title),
                )
                Text(
                    text = stringResource(Res.string.dashboard_empty_secondary_cta_subtitle),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}