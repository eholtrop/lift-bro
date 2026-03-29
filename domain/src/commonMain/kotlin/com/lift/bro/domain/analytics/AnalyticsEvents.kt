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
        const val WRAPPED_LANDING = "screen_wrapped_landing"
        const val WRAPPED_TENURE = "screen_wrapped_tenure"
        const val WRAPPED_CONSISTENCY = "screen_wrapped_consistency"
        const val WRAPPED_WEIGHT = "screen_wrapped_weight"
        const val WRAPPED_EXERCISES = "screen_wrapped_exercises"
        const val WRAPPED_TOP_LIFTS = "screen_wrapped_top_lifts"
        const val ONBOARDING = "screen_onboarding"
    }

    object Actions {
        const val TAB_SWITCHED = "tab_switched"
        const val FAB_CLICKED = "fab_clicked"
        const val DASHBOARD_BUTTON_CLICKED = "dashboard_button_clicked"
        const val CALENDAR_BUTTON_CLICKED = "calendar_button_clicked"

        const val ONBOARDING_BRO_SELECTED = "onboarding_bro_selected"
        const val ONBOARDING_CONTINUE_TAPPED = "onboarding_continue_tapped"
        const val ONBOARDING_SKIP_TAPPED = "onboarding_skip_tapped"
        const val ONBOARDING_COMPLETED = "onboarding_completed"

        const val SETTINGS_BRO_CHANGED = "settings_bro_changed"
        const val SETTINGS_BACKUP_TAPPED = "settings_backup_tapped"
        const val SETTINGS_THEME_CHANGED = "settings_theme_changed"
        const val SETTINGS_SUBSCRIPTION_TAPPED = "settings_subscription_tapped"

        const val LIFT_COLOR_PICKER_OPENED = "lift_color_picker_opened"
        const val LIFT_COLOR_CHANGED = "lift_color_changed"
        const val LIFT_NAME_EDITED = "lift_name_edited"
        const val LIFT_DELETED = "lift_deleted"
        const val VARIATION_CREATED = "variation_created"

        const val VARIATION_ADD_SET_FAB_CLICKED = "variation_add_set_fab_clicked"
        const val VARIATION_EDIT_TAPPED = "variation_edit_tapped"
        const val VARIATION_DELETED = "variation_deleted"

        const val SET_VARIATION_SELECTED = "set_variation_selected"
        const val SET_SAVED = "set_saved"
        const val SET_DELETED = "set_deleted"
    }

    object Properties {
        const val FROM_TAB = "from_tab"
        const val TO_TAB = "to_tab"
        const val SCREEN = "screen"

        const val BRO = "bro"
        const val STEP = "step"

        const val NEW_BRO = "new_bro"
        const val THEME = "theme"

        const val LIFT_ID = "lift_id"
        const val LIFT_NAME = "lift_name"
        const val VARIATION_COUNT = "variation_count"
        const val VARIATION_ID = "variation_id"
        const val VARIATION_NAME = "variation_name"

        const val IS_NEW = "is_new"
        const val VARIATION_ID_PROPERTY = "variation_id"
        const val SOURCE = "source"

        const val DATE = "date"
        const val EXERCISE_COUNT = "exercise_count"
        const val HAS_NOTES = "has_notes"
        const val HAS_WARMUP = "has_warmup"
        const val WORKOUT_DURATION_MINUTES = "workout_duration_minutes"

        const val GOAL_ID = "goal_id"
        const val HAS_TEXT = "has_text"
        const val IS_ACHIEVED = "is_achieved"
        const val GOAL_COUNT = "goal_count"
        const val ACHIEVED_COUNT = "achieved_count"

        const val STATE = "state"
        const val TEMPO = "tempo"
        const val REPS = "reps"
        const val TOTAL_DURATION_SECONDS = "total_duration_seconds"
        const val SET_COUNT = "set_count"
        const val ELAPSED_SECONDS = "elapsed_seconds"
        const val REMAINING_SECONDS = "remaining_seconds"

        const val YEAR = "year"

        const val UNIT_OF_MEASURE = "unit_of_measure"
        const val THEME_MODE = "theme_mode"
        const val SUBSCRIPTION_TYPE = "subscription_type"
        const val PLATFORM = "platform"
        const val APP_VERSION = "app_version"
    }
}
