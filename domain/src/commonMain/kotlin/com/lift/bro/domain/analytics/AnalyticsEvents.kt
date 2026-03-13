package com.lift.bro.domain.analytics

object AnalyticsEvents {
    object Screens {
        const val HOME_DASHBOARD = "screen_home_dashboard"
        const val HOME_CALENDAR = "screen_home_calendar"
        const val SETTINGS = "screen_settings"
        const val LIFT_DETAILS = "screen_lift_details"
        const val VARIATION_DETAILS = "screen_variation_details"
        const val EDIT_SET = "screen_edit_set"
        const val WORKOUT = "screen_workout"
        const val GOALS = "screen_goals"
        const val TIMER = "screen_timer"
        const val WRAPPED = "screen_wrapped"
        const val ONBOARDING = "screen_onboarding"
    }

    object Actions {
        const val SET_CREATED = "set_created"
        const val SET_EDITED = "set_edited"
        const val SET_DELETED = "set_deleted"
        const val WORKOUT_STARTED = "workout_started"
        const val WORKOUT_COMPLETED = "workout_completed"
        const val WORKOUT_DELETED = "workout_deleted"
        const val GOAL_CREATED = "goal_created"
        const val GOAL_ACHIEVED = "goal_achieved"
        const val GOAL_DELETED = "goal_deleted"
        const val BACKUP_CREATED = "backup_created"
        const val BACKUP_RESTORED = "backup_restored"
        const val SUBSCRIPTION_STARTED = "subscription_started"
        const val SUBSCRIPTION_CANCELLED = "subscription_cancelled"
    }

    object Properties {
        const val UNIT_OF_MEASURE = "unit_of_measure"
        const val THEME_MODE = "theme_mode"
        const val SUBSCRIPTION_TYPE = "subscription_type"
        const val PLATFORM = "platform"
        const val APP_VERSION = "app_version"
    }
}
