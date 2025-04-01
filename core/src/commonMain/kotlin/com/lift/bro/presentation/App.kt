package com.lift.bro.presentation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.compose.AppTheme
import com.lift.bro.config.BuildConfig
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.presentation.dialog.BackupDialog
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.home.DashboardViewModel
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.lift.toLocalDate
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.variation.EditVariationScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    object Dashboard : Destination

    @Serializable
    data class LiftDetails(val liftId: String) : Destination

    @Serializable
    data class EditLift(val liftId: String?) : Destination

    @Serializable
    data class VariationDetails(val variationId: String) : Destination

    @Serializable
    data class EditVariation(val variationId: String) : Destination

    @Serializable
    data class EditSet(
        val setId: String? = null,
        val liftId: String? = null,
        val variationId: String? = null
    ) : Destination

    @Serializable
    object Settings : Destination
}

val LocalNavController = compositionLocalOf<NavHostController>() {
    error("NavHostController was not set")
}

@Composable
fun App(
    navController: NavHostController = rememberNavController()
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

            CompositionLocalProvider(
                LocalNavController provides navController
            ) {
                NavHost(
                    navController = navController,
                    startDestination = Destination.Dashboard,
                    enterTransition = {
                        slideInHorizontally {
                            it / 2
                        }
                    },
                    exitTransition = {
                        slideOutHorizontally {
                            it / 2
                        }
                    }
                ) {
                    composable<Destination.Dashboard> { entry ->
                        DashboardScreen(
                            viewModel = DashboardViewModel(),
                            addLiftClicked = {
                                navController.navigate(Destination.EditLift(null))
                            },
                            liftClicked = {
                                navController.navigate(Destination.LiftDetails(it.id))
                            },
                            addSetClicked = {
                                navController.navigate(Destination.EditSet(null, null, null))
                            },
                            setClicked = {
                                navController.navigate(Destination.EditSet(it.id, null, null))
                            }
                        )
                    }

                    composable<Destination.LiftDetails> {
                        val route: Destination.LiftDetails = it.toRoute()
                        LiftDetailsScreen(
                            liftId = route.liftId,
                            editLiftClicked = {
                                navController.navigate(Destination.EditLift(route.liftId))
                            },
                            variationClicked = {
                                navController.navigate(
                                    Destination.VariationDetails(
                                        variationId = it
                                    )
                                )
                            },
                            addSetClicked = {
                                navController.navigate(Destination.EditSet(liftId = route.liftId))
                            },
                            onSetClicked = {
                                navController.navigate(Destination.EditSet(setId = it.id))
                            },
                        )
                    }

                    composable<Destination.EditLift> {
                        val route: Destination.EditLift = it.toRoute()
                        EditLiftScreen(
                            liftId = route.liftId,
                            liftSaved = {
                                navController.popBackStack()
                            },
                        )
                    }

                    composable<Destination.Settings> {
                        SettingsScreen()
                    }

                    composable<Destination.VariationDetails> {
                        val route: Destination.VariationDetails = it.toRoute()
                        VariationDetailsScreen(
                            variationId = route.variationId,
                            addSetClicked = {
                                navController.navigate(
                                    Destination.EditSet(
                                        null,
                                        null,
                                        route.variationId
                                    )
                                )
                            },
                            editClicked = {
                                navController.navigate(
                                    Destination.EditVariation(
                                        variationId = route.variationId,
                                    )
                                )
                            },
                            setClicked = {
                                navController.navigate(
                                    Destination.EditSet(
                                        setId = it.id, null, null
                                    )
                                )
                            }
                        )
                    }

                    composable<Destination.EditVariation> {
                        val route: Destination.EditVariation = it.toRoute()
                        EditVariationScreen(
                            id = route.variationId,
                            variationSaved = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable<Destination.EditSet> {
                        val route: Destination.EditSet = it.toRoute()
                        EditSetScreen(
                            setId = route.setId,
                            variationId = route.variationId,
                            liftId = route.liftId,
                            setSaved = {
                                navController.popBackStack()
                            },
                            createLiftClicked = {
                                navController.navigate(Destination.EditLift(null))
                            },
                        )
                    }
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
