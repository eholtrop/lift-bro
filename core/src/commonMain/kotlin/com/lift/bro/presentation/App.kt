package com.lift.bro.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import com.example.compose.AppTheme
import com.lift.bro.config.BuildConfig
import com.lift.bro.domain.models.CelebrationType
import com.lift.bro.domain.usecases.GetCelebrationTypeUseCase
import com.lift.bro.presentation.excercise.ExcerciseDetailsScreen
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.home.DashboardViewModel
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import com.lift.bro.ui.ConfettiExplosion
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.BackupAlertDialog
import com.lift.bro.ui.dialog.EditVariationDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.NavCoordinator
import com.lift.bro.ui.navigation.SwipeableNavHost
import com.lift.bro.ui.navigation.rememberNavCoordinator
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest


@Composable
fun App(
    navCoordinator: NavCoordinator = rememberNavCoordinator(Destination.Dashboard)
) {
    AppTheme {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {


            LaunchedEffect("debug_mode") {
                if (BuildConfig.isDebug) {
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

            BackupAlertDialog()

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
                            setClicked = { variation, date ->
                                navCoordinator.present(Destination.EditExcercise(variationId = variation.id, localDate =  date))
                            }
                        )

                    is Destination.EditExcercise -> ExcerciseDetailsScreen(
                        date = route.localDate,
                        variationId = route.variationId
                    )

                    is Destination.EditLift -> EditLiftScreen(
                        liftId = route.liftId,
                        liftSaved = {
                            navCoordinator.onBackPressed()
                        },
                        liftDeleted = {
                            navCoordinator.popToRoot(false)
                        },
                        editVariationClicked = {
                            navCoordinator.present(Destination.EditVariation(variationId = it.id))
                        }
                    )

                    is Destination.EditSet ->
                        EditSetScreen(
                            setId = route.setId,
                            variationId = route.variationId,
                            liftId = route.liftId,
                            setSaved = {
                                navCoordinator.onBackPressed(keepStack = false)
                            },
                            createLiftClicked = {
                                navCoordinator.present(Destination.EditLift(null))
                            },
                        )

                    is Destination.EditVariation -> {
                        EditVariationDialog(
                            variationId = route.variationId,
                            onDismissRequest = {
                                navCoordinator.onBackPressed(false)
                            },
                            onVariationSaved = {
                                navCoordinator.onBackPressed(false)
                            },
                        )
                    }

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

            // This is all pretty terrible.... but its something I promised a friend id release before they hit PR!... and I got bugs to fix
            var showCelebration by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                GetCelebrationTypeUseCase()
                    .collectLatest {
                        if (it != CelebrationType.None) {
                            showCelebration = true
                        }
                    }
            }

            if (showCelebration) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    ConfettiExplosion()

                    var showMessage by remember { mutableStateOf(false) }
                    AnimatedVisibility(
                        modifier = Modifier.align(Alignment.Center),
                        visible = showMessage,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                        exit = fadeOut(),
                    ) {
                        Column(
                            modifier = Modifier
                                .semantics(
                                    mergeDescendants = true,
                                ) {
                                    liveRegion = LiveRegionMode.Assertive
                                }
                                .background(
                                    MaterialTheme.colorScheme.primary,
                                    shape = MaterialTheme.shapes.medium
                                )
                                .padding(MaterialTheme.spacing.one),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Text(
                                text = "Congrats!!",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                            )
                            Space(MaterialTheme.spacing.half)
                            Text(
                                text = "That's a new Personal Record!",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    LaunchedEffect(Unit) {
                        delay(1000)
                        showMessage = true
                        delay(4000)
                        showMessage = false
                        delay(2000)
                        showCelebration = false
                    }
                }
            }
        }
    }
}
