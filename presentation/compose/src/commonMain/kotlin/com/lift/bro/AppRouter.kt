package com.lift.bro

import androidx.compose.runtime.Composable
import com.lift.bro.config.BuildConfig
import com.lift.bro.presentation.category.CategoryDetailsScreen
import com.lift.bro.presentation.goals.GoalsScreen
import com.lift.bro.presentation.home.HomeScreen
import com.lift.bro.presentation.onboarding.OnboardingScreen
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.timer.TimerScreen
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

        is Destination.CreateLift -> CategoryDetailsScreen(
            liftId = route.liftId,
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
            CategoryDetailsScreen(
                liftId = route.liftId,
            )

        Destination.Settings -> SettingsScreen()
        is Destination.MovementDetails ->
            VariationDetailsScreen(
                variationId = route.movementId,
                addSetClicked = {
                    navCoordinator.present(
                        CreateSet(
                            variationId = route.movementId
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
        is Destination.CreateMovement ->
            VariationDetailsScreen(
                variationId = route.movementId,
                addSetClicked = {
                    navCoordinator.present(
                        CreateSet(
                            variationId = route.movementId
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

        is Destination.Timer -> when (route) {
            is Destination.Timer.From -> TimerScreen(route.setId)
            is Destination.Timer.With -> TimerScreen(
                tempo = route.tempo,
                reps = route.reps,
            )
        }
    }
}
