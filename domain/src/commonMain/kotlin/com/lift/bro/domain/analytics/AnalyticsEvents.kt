package com.lift.bro.domain.analytics

object AnalyticsEvents {
    object Screens {
        const val HOME_DASHBOARD = "screen_home_dashboard"
        const val HOME_CALENDAR = "screen_home_calendar"
        const val SETTINGS = "screen_settings"
        const val CATEGORY_DETAILS = "screen_category_details"
        const val CATEGORY_CREATE = "screen_category_create"
        const val MOVEMENT_DETAILS = "screen_movement_details"
        const val MOVEMENT_CREATE = "screen_movement_create"
        const val EDIT_SET = "screen_set_edit"
        const val CREATE_SET = "screen_set_create"
        const val WORKOUT = "screen_workout"
        const val GOALS = "screen_goals"
        const val TIMER = "screen_timer"
        const val WRAPPED_LANDING = "screen_wrapped_landing"
        const val ONBOARDING = "screen_onboarding"
    }

    object Actions {
        const val TAB_SWITCHED = "tab_switched"
        const val BUTTON_CLICKED = "button_clicked"
        const val FAB_CLICKED = "fab_clicked"

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
        const val LIFT_FAVOURITE_TOGGLED = "lift_favourite_toggled"

        const val VARIATION_ADD_SET_FAB_CLICKED = "variation_add_set_fab_clicked"
        const val VARIATION_EDIT_TAPPED = "variation_edit_tapped"
        const val VARIATION_DELETED = "variation_deleted"
        const val VARIATION_NAME_EDITED = "variation_name_edited"

        const val SET_VARIATION_SELECTED = "set_variation_selected"
        const val SET_SAVED = "set_saved"
        const val SET_DELETED = "set_deleted"
        const val SET_REP_CHANGED = "set_rep_changed"
        const val SET_WEIGHT_CHANGED = "set_weight_changed"
        const val SET_RPE_CHANGED = "set_rpe_changed"
        const val SET_TEMPO_CHANGED = "set_tempo_changed"
        const val SET_NOTES_EDITED = "set_notes_edited"
        const val SET_DATE_CHANGED = "set_date_changed"
        const val SET_V2_TOGGLED = "set_v2_toggled"

        const val DASHBOARD_SORTING_CHANGED = "dashboard_sorting_changed"
        const val DASHBOARD_FAVOURITES_TOGGLED = "dashboard_favourites_toggled"

        const val TIMER_STARTED = "timer_started"
        const val TIMER_PAUSED = "timer_paused"
        const val TIMER_RESUMED = "timer_resumed"
        const val TIMER_STOPPED = "timer_stopped"
        const val TIMER_ENDED = "timer_ended"
        const val TIMER_RESTARTED = "timer_restarted"
        const val TIMER_ADDED = "timer_added"
        const val TIMER_REMOVED = "timer_removed"
        const val TIMER_STARTUP_TIME_CHANGED = "timer_startup_time_changed"
        const val TIMER_TEMPO_CHANGED = "timer_tempo_changed"
        const val TIMER_REST_CHANGED = "timer_rest_changed"
        const val TIMER_AUDIO_TOGGLED = "timer_audio_toggled"

        const val GOAL_ADDED = "goal_added"
        const val GOAL_DELETED = "goal_deleted"
        const val GOAL_EDITED = "goal_edited"
        const val GOAL_ACHIEVED_TOGGLED = "goal_achieved_toggled"

        const val WORKOUT_NOTES_UPDATED = "workout_notes_updated"
        const val WORKOUT_EXERCISE_ADDED = "workout_exercise_added"
        const val WORKOUT_SUPERSET_ADDED = "workout_superset_added"
        const val WORKOUT_FINISHER_UPDATED = "workout_finisher_updated"
        const val WORKOUT_WARMUP_UPDATED = "workout_warmup_updated"
        const val WORKOUT_SET_DUPLICATED = "workout_set_duplicated"
        const val WORKOUT_SET_DELETED = "workout_set_deleted"
        const val WORKOUT_EXERCISE_DELETED = "workout_exercise_deleted"
        const val WORKOUT_COPIED = "workout_copied"
        const val WORKOUT_VARIATION_DELETED = "workout_variation_deleted"

        const val CALENDAR_ADD_WORKOUT_CLICKED = "calendar_add_workout_clicked"
        const val CALENDAR_WORKOUT_CLICKED = "calendar_workout_clicked"
        const val CALENDAR_DATE_SELECTED = "calendar_date_selected"
        const val CALENDAR_ADD_TO_WORKOUT = "calendar_add_to_workout"

        const val SETTINGS_URL_UPDATED = "settings_url_updated"
        const val SETTINGS_MODE_APPLIED = "settings_mode_applied"

        const val SERVER_TURNED_ON = "server_turned_on"
        const val SERVER_TURNED_OFF = "server_turned_off"
        const val SERVER_STATUS_UPDATED = "server_status_updated"

        const val CALCULATOR_DIGIT_ADDED = "calculator_digit_added"
        const val CALCULATOR_OPERATOR_SELECTED = "calculator_operator_selected"
        const val CALCULATOR_ACTION_APPLIED = "calculator_action_applied"
        const val CALCULATOR_UOM_TOGGLED = "calculator_uom_toggled"

        const val VARIATION_SEARCH_QUERY_CHANGED = "variation_search_query_changed"
        const val VARIATION_SEARCH_SELECTED = "variation_search_selected"
        const val VARIATION_SEARCH_DISMISSED = "variation_search_dismissed"

        const val VARIATION_NOTES_EDITED = "variation_notes_edited"
        const val VARIATION_NAME_UPDATED = "variation_name_updated"
        const val VARIATION_BODYWEIGHT_TOGGLED = "variation_bodyweight_toggled"

        const val DAILY_CREATE_WORKOUT_CLICKED = "daily_create_workout_clicked"
        const val DAILY_OPEN_WORKOUT_CLICKED = "daily_open_workout_clicked"
        const val DAILY_ADD_TO_WORKOUT = "daily_add_to_workout"

        const val WRAPPED_GOAL_ADDED = "wrapped_goal_added"
        const val WRAPPED_GOAL_REMOVED = "wrapped_goal_removed"
        const val WRAPPED_GOAL_NAME_CHANGED = "wrapped_goal_name_changed"
    }

    object Properties {
        const val FROM_TAB = "from_tab"
        const val TO_TAB = "to_tab"
        const val SCREEN = "screen"
        const val BUTTON_NAME = "button_name"

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
