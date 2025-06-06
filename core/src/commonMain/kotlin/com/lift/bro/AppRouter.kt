package com.lift.bro

import androidx.compose.runtime.Composable
import com.lift.bro.presentation.excercise.ExcerciseDetailsScreen
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.home.DashboardViewModel
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import com.lift.bro.ui.dialog.EditVariationDialog
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator

@Composable
fun AppRouter(route: Destination) {
    val navCoordinator = LocalNavCoordinator.current
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