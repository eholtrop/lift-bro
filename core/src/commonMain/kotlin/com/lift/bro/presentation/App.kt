package com.lift.bro.presentation

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.compose.AppTheme
import com.lift.bro.config.BuildConfig
import com.lift.bro.data.Backup
import com.lift.bro.data.BackupRestore
import com.lift.bro.data.LBDatabase
import com.lift.bro.debugBackup
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.variation.EditVariationScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import kotlinx.coroutines.flow.collect
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
    data class EditVariation(val variationId: String?, val parentLiftId: String?) : Destination

    @Serializable
    data class EditSet(val setId: String?, val liftId: String?, val variationId: String?) :
        Destination
}

@Composable
fun App(
    navController: NavHostController = rememberNavController()
) {
    AppTheme() {
        Box(
            modifier = Modifier.background(MaterialTheme.colorScheme.background)
        ) {

//            LaunchedEffect("debug_mode") {
//                if (BuildConfig.DEBUG) {
//                    BackupRestore.restore(
//                        backup = debugBackup
//                    ).collect()
//                }
//            }

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
                        addVariationClicked = {
                            navController.navigate(
                                Destination.EditVariation(
                                    variationId = null,
                                    parentLiftId = it.id
                                )
                            )
                        },
                        variationClicked = {
                            navController.navigate(
                                Destination.EditVariation(
                                    variationId = it,
                                    parentLiftId = null
                                )
                            )
                        },
                        addSetClicked = {
                            navController.navigate(Destination.EditSet(null, it.id, null))
                        },
                        onSetClicked = {
                            navController.navigate(Destination.EditSet(it.id, null, null))
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

                composable<Destination.VariationDetails> {
                    val route: Destination.VariationDetails = it.toRoute()
                    VariationDetailsScreen(
                        variationId = route.variationId,
                        addSetClicked = {
                            navController.navigate(Destination.EditSet(null, null, null))
                        },
                        editClicked = {
                            navController.navigate(Destination.EditVariation(variationId = route.variationId, parentLiftId = null))
                        },
                        setClicked = {
                            navController.navigate(Destination.EditSet(it.id, null, null))
                        }
                    )
                }

                composable<Destination.EditVariation> {
                    val route: Destination.EditVariation = it.toRoute()
                    EditVariationScreen(
                        id = route.variationId,
                        parentLiftId = route.parentLiftId,
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
                        createVariationClicked = {

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
