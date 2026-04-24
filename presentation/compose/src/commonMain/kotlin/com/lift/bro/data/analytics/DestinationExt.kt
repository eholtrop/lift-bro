package com.lift.bro.data.analytics

import com.lift.bro.domain.analytics.AnalyticsEvents
import com.lift.bro.ui.navigation.Destination

val Destination.screenName: String?
    get() = when (this) {
        Destination.Onboarding -> AnalyticsEvents.Screens.ONBOARDING
        Destination.Home -> AnalyticsEvents.Screens.HOME_DASHBOARD
        is Destination.Timer -> AnalyticsEvents.Screens.TIMER
        is Destination.CreateCategory -> AnalyticsEvents.Screens.CATEGORY_CREATE
        is Destination.CategoryDetails -> AnalyticsEvents.Screens.CATEGORY_DETAILS
        is Destination.EditSet -> AnalyticsEvents.Screens.EDIT_SET
        is Destination.CreateSet -> AnalyticsEvents.Screens.CREATE_SET
        is Destination.MovementDetails -> AnalyticsEvents.Screens.MOVEMENT_DETAILS
        is Destination.CreateMovement -> AnalyticsEvents.Screens.MOVEMENT_CREATE
        is Destination.EditWorkout -> AnalyticsEvents.Screens.WORKOUT
        is Destination.CreateWorkout -> AnalyticsEvents.Screens.WORKOUT
        is Destination.Wrapped -> AnalyticsEvents.Screens.WRAPPED_LANDING
        Destination.Settings -> AnalyticsEvents.Screens.SETTINGS
        Destination.Goals -> AnalyticsEvents.Screens.GOALS
        is Destination.Unknown -> null
    }
