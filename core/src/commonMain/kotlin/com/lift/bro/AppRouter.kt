package com.lift.bro

import androidx.compose.runtime.Composable
import com.lift.bro.config.BuildConfig
import com.lift.bro.presentation.home.HomeScreen
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.onboarding.OnboardingScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.variation.VariationDetailsScreen
import com.lift.bro.presentation.workout.CreateWorkoutScreen
import com.lift.bro.presentation.workout.rememberWorkoutInteractor
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d

@Composable
fun AppRouter(route: Destination) {
    val navCoordinator = LocalNavCoordinator.current
    if (BuildConfig.isDebug) {
        Log.d("DEBUGEH", "route: $route")
    }

    when (route) {
        is Destination.Unknown -> {}

        is Destination.Onboarding -> OnboardingScreen()

        Destination.Home -> HomeScreen()

        is Destination.EditWorkout -> CreateWorkoutScreen(
            interactor = rememberWorkoutInteractor(route.localDate),
        )
        is Destination.CreateWorkout -> CreateWorkoutScreen(rememberWorkoutInteractor(route.localDate))

        is Destination.EditLift -> EditLiftScreen(
            liftId = route.liftId,
            liftSaved = {
                navCoordinator.onBackPressed(keepStack = false)
            },
            liftDeleted = {
                navCoordinator.popToRoot(false)
            },
        )

        is Destination.EditSet ->
            EditSetScreen(
                setId = route.setId,
                variationId = route.variationId,
                liftId = route.liftId,
                date = route.date,
                setSaved = {
                    navCoordinator.onBackPressed(keepStack = false)
                },
                createLiftClicked = {
                    navCoordinator.present(Destination.EditLift(null))
                },
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