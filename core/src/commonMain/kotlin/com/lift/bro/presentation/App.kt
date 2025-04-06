package com.lift.bro.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.compose.AppTheme
import com.lift.bro.config.BuildConfig
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.dialog.BackupDialog
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.home.DashboardViewModel
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.navigation.Destination
import com.lift.bro.presentation.navigation.NavCoordinator
import com.lift.bro.presentation.navigation.SwipeableNavHost
import com.lift.bro.presentation.navigation.rememberNavCoordinator
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.variation.EditVariationScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.daysUntil


@Composable
fun App(
    navCoordinator: NavCoordinator = rememberNavCoordinator(Destination.Dashboard)
) {
    AppTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {

            var showBackupModal by remember { mutableStateOf(false) }

            LaunchedEffect("debug_mode") {
                if (BuildConfig.DEBUG) {
//                    BackupRestore.restore(
//                        backup = debugBackup
//                    ).collect()

//                    dependencies.settingsRepository.saveBackupSettings(
//                        BackupSettings(
//                            lastBackupDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
//                                .minus(8, DateTimeUnit.DAY)
//                        )
//                    )
                }
            }

            LaunchedEffect("check_for_update_prompt") {
                val backupSettings = dependencies.settingsRepository.getBackupSettings().first()
                showBackupModal =
                    backupSettings.lastBackupDate.daysUntil(Clock.System.now().toLocalDate()) >= 7
            }

            if (showBackupModal) {
                BackupDialog(
                    onDismissRequest = { showBackupModal = false }
                )
            }


            SwipeableNavHost(
                navCoordinator = navCoordinator,
            ) { route ->

                when (route) {
                    Destination.Dashboard ->
                        DashboardScreen(
                            viewModel = DashboardViewModel(),
                            addLiftClicked = {
                                navCoordinator.present(Destination.EditLift(null))
                            },
                            liftClicked = {
                                navCoordinator.present(Destination.LiftDetails(it.id))
                            },
                            addSetClicked = {
                                navCoordinator.present(Destination.EditSet(null, null, null))
                            },
                            setClicked = {
                                navCoordinator.present(Destination.EditSet(it.id, null, null))
                            }
                        )

                    is Destination.EditLift -> EditLiftScreen(
                        liftId = route.liftId,
                        liftSaved = {
                            navCoordinator.onBackPressed()
                        },
                    )

                    is Destination.EditSet ->
                        EditSetScreen(
                            setId = route.setId,
                            variationId = route.variationId,
                            liftId = route.liftId,
                            setSaved = {
                                navCoordinator.onBackPressed()
                            },
                            createLiftClicked = {
                                navCoordinator.present(Destination.EditLift(null))
                            },
                        )

                    is Destination.EditVariation ->
                        EditVariationScreen(
                            id = route.variationId,
                            variationSaved = {
                                navCoordinator.onBackPressed()
                            }
                        )

                    is Destination.LiftDetails ->
                        LiftDetailsScreen(
                            liftId = route.liftId,
                            editLiftClicked = {
                                navCoordinator.present(Destination.EditLift(route.liftId))
                            },
                            variationClicked = {
                                navCoordinator.present(
                                    Destination.VariationDetails(
                                        variationId = it
                                    )
                                )
                            },
                            addSetClicked = {
                                navCoordinator.present(Destination.EditSet(liftId = route.liftId))
                            },
                            onSetClicked = {
                                navCoordinator.present(Destination.EditSet(setId = it.id))
                            },
                        )

                    Destination.Settings -> SettingsScreen()
                    is Destination.VariationDetails ->
                        VariationDetailsScreen(
                            variationId = route.variationId,
                            addSetClicked = {
                                navCoordinator.present(
                                    Destination.EditSet(
                                        null,
                                        null,
                                        route.variationId
                                    )
                                )
                            },
                            editClicked = {
                                navCoordinator.present(
                                    Destination.EditVariation(
                                        variationId = route.variationId,
                                    )
                                )
                            },
                            setClicked = {
                                navCoordinator.present(
                                    Destination.EditSet(
                                        setId = it.id, null, null
                                    )
                                )
                            }
                        )
                }
            }
        }
    }
}

object Spacing {
    val two = 32.dp
    val oneAndHalf = 24.dp
    val one = 16.dp
    val half = 8.dp
    val quarter = 4.dp
}

object LiftingTheme {
    val spacing: Spacing = Spacing
}

val MaterialTheme.spacing get() = LiftingTheme.spacing
