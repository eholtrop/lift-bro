package com.lift.bro

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.lift.bro.config.BuildConfig
import com.lift.bro.data.analytics.screenName
import com.lift.bro.di.dependencies
import com.lift.bro.domain.analytics.Analytics
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.category.CategoryDetailsScreen
import com.lift.bro.presentation.goals.GoalsScreen
import com.lift.bro.presentation.home.HomeScreen
import com.lift.bro.presentation.movement.CreateMovementScreen
import com.lift.bro.presentation.movement.MovementDetailsScreen
import com.lift.bro.presentation.onboarding.OnboardingScreen
import com.lift.bro.presentation.recording.RecordSetScreen
import com.lift.bro.presentation.recording.rememberRecordingInteractor
import com.lift.bro.presentation.set.EditSetScreen
import com.lift.bro.presentation.settings.SettingsScreen
import com.lift.bro.presentation.workout.WorkoutScreen
import com.lift.bro.presentation.workout.rememberWorkoutInteractor
import com.lift.bro.presentation.wrapped.WrappedLandingScreen
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.Destination.CreateSet
import com.lift.bro.ui.navigation.Destination.EditSet
import tv.dpal.logging.Log
import tv.dpal.logging.d
import tv.dpal.navi.NavCoordinator

@Composable
fun AppRouter(
    route: Destination,
    navCoordinator: NavCoordinator<Destination> = LocalNavCoordinator.current,
    analytics: Analytics = dependencies.analytics,
) {
    if (BuildConfig.isDebug) {
        Log.d("App Router Navigation", "route: $route")
    }

    LaunchedEffect(route) {
        route.screenName?.let {
            analytics.trackScreenView(it)
        }
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

        is Destination.CreateCategory -> CategoryDetailsScreen(
            liftId = route.liftId,
        )

        is Destination.CategoryDetails ->
            CategoryDetailsScreen(
                liftId = route.liftId,
            )

        is EditSet ->
            EditSetScreen(
                setId = route.setId,
            )

        is CreateSet -> EditSetScreen(
            variationId = route.movementId,
            date = route.date,
            sectionId = route.sectionId
        )

        Destination.Settings -> SettingsScreen()

        is Destination.MovementDetails ->
            MovementDetailsScreen(
                movementId = route.movementId,
                addSetClicked = {
                    navCoordinator.present(
                        CreateSet(
                            movementId = route.movementId
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
            CreateMovementScreen(
                movementId = route.movementId,
                categoryId = route.categoryId,
                addSetClicked = {
                    navCoordinator.present(
                        CreateSet(
                            movementId = route.movementId
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

        is Destination.Recording -> RecordSetScreen(
            interactor = rememberRecordingInteractor(setId = route.setId)
        )
    }
}
