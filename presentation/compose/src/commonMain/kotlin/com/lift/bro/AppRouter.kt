package com.lift.bro

import androidx.compose.runtime.Composable
import com.lift.bro.config.BuildConfig
import com.lift.bro.presentation.goals.GoalsScreen
import com.lift.bro.presentation.home.HomeScreen
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.onboarding.OnboardingScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.timer.TimerScreen
import com.lift.bro.presentation.timer.TimerState
import com.lift.bro.presentation.variation.VariationDetailsScreen
import com.lift.bro.presentation.workout.WorkoutScreen
import com.lift.bro.presentation.workout.rememberWorkoutInteractor
import com.lift.bro.presentation.wrapped.WrappedLandingScreen
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import tv.dpal.logging.Log
import tv.dpal.logging.d
import tv.dpal.navi.LocalNavCoordinator

@Composable
fun AppRouter(route: Destination) {
    val navCoordinator = LocalNavCoordinator.current
    if (BuildConfig.isDebug) {
        Log.d("App Router Navigation", "route: $route")
    }

    when (route) {
        is Destination.Unknown -> {}

        is Destination.Wrapped -> WrappedLandingScreen(
            year = route.year,
            onClosePressed = {
                navCoordinator.onBackPressed(keepStack = false)
            },
        )

        is Destination.Onboarding -> OnboardingScreen()

        Destination.Home -> HomeScreen()

        is Destination.EditWorkout -> WorkoutScreen(
            interactor = rememberWorkoutInteractor(route.localDate),
        )

        is Destination.CreateWorkout -> WorkoutScreen(rememberWorkoutInteractor(route.localDate))

        is Destination.EditLift -> EditLiftScreen(
            liftId = route.liftId,
            liftSaved = {
                navCoordinator.onBackPressed(keepStack = false)
            },
            liftDeleted = {
                navCoordinator.popToRoot(false)
            },
        )

        is EditSet ->
            EditSetScreen(
                setId = route.setId,
            )

        is CreateSet -> EditSetScreen(
            variationId = route.variationId,
            date = route.date,
        )

        is Destination.LiftDetails ->
            LiftDetailsScreen(
                liftId = route.liftId,
            )

        Destination.Settings -> SettingsScreen()
        is Destination.VariationDetails ->
            VariationDetailsScreen(
                variationId = route.variationId,
                addSetClicked = {
                    navCoordinator.present(
                        CreateSet(
                            variationId = route.variationId
                        )
                    )
                },
                setClicked = {
                    navCoordinator.present(
                        EditSet(
                            setId = it.id
                        )
                    )
                }
            )

        Destination.Goals -> {
            GoalsScreen()
        }

        is Destination.Timer -> TimerScreen()
    }
}
