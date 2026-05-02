package com.lift.bro.data.analytics

import com.lift.bro.domain.analytics.AnalyticsEvents
import com.lift.bro.ui.navigation.Destination

val Destination.screenName: String?
    get() = when (this) {
        Destination.Onboarding -> AnalyticsEvents.Screens.ONBOARDING
        Destination.Home -> AnalyticsEvents.Screens.HOME_DASHBOARD
        is Destination.Timer -> AnalyticsEvents.Screens.TIMER
        is Destination.LiftDetails -> AnalyticsEvents.Screens.LIFT_DETAILS
        is Destination.EditLift -> AnalyticsEvents.Screens.EDIT_LIFT
        is Destination.VariationDetails -> AnalyticsEvents.Screens.VARIATION_DETAILS
        is Destination.EditSet -> AnalyticsEvents.Screens.EDIT_SET
        is Destination.CreateSet -> AnalyticsEvents.Screens.EDIT_SET
        is Destination.EditWorkout -> AnalyticsEvents.Screens.WORKOUT
        is Destination.CreateWorkout -> AnalyticsEvents.Screens.WORKOUT
        Destination.Settings -> AnalyticsEvents.Screens.SETTINGS
        is Destination.Wrapped -> AnalyticsEvents.Screens.WRAPPED_LANDING
        Destination.Goals -> AnalyticsEvents.Screens.GOALS
        is Destination.Unknown -> null
    }
