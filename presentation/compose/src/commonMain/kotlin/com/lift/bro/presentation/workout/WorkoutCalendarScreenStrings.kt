package com.lift.bro.presentation.workout

import androidx.compose.runtime.Composable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_fab_content_description
import lift_bro.core.generated.resources.edit_daily_notes_dialog_confirm_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_dismiss_cta
import lift_bro.core.generated.resources.edit_daily_notes_dialog_placeholder
import lift_bro.core.generated.resources.edit_daily_notes_dialog_title
import lift_bro.core.generated.resources.workout_calendar_edit_daily_notes_cta
import lift_bro.core.generated.resources.workout_calendar_screen_favourite_content_description
import lift_bro.core.generated.resources.workout_calendar_screen_finisher_label
import lift_bro.core.generated.resources.workout_calendar_screen_other_gains_title
import lift_bro.core.generated.resources.workout_calendar_screen_start_workout_cta
import lift_bro.core.generated.resources.workout_calendar_screen_warmup_label
import org.jetbrains.compose.resources.stringResource

data class WorkoutCalendarScreenStrings(
    val notesSaveButton: String,
    val notesCancelButton: String,
    val notesPlaceholder: String,
    val notesDialogTitle: String,
    val editNotesContentDescription: String,
    val startWorkoutButton: String,
    val otherGainsTitle: String,
    val otherGainsSubtitle: String,
    val warmupLabel: String,
    val finisherLabel: String,
    val favoriteContentDescription: String,
) {
    companion object {
        @Composable
        fun default(): WorkoutCalendarScreenStrings = WorkoutCalendarScreenStrings(
            notesSaveButton = stringResource(Res.string.edit_daily_notes_dialog_confirm_cta),
            notesCancelButton = stringResource(Res.string.edit_daily_notes_dialog_dismiss_cta),
            notesPlaceholder = stringResource(Res.string.edit_daily_notes_dialog_placeholder),
            notesDialogTitle = stringResource(Res.string.edit_daily_notes_dialog_title),
            editNotesContentDescription = stringResource(Res.string.workout_calendar_edit_daily_notes_cta),
            startWorkoutButton = stringResource(Res.string.workout_calendar_screen_start_workout_cta),
            otherGainsTitle = stringResource(Res.string.workout_calendar_screen_other_gains_title),
            otherGainsSubtitle = stringResource(Res.string.workout_calendar_screen_warmup_label),
            warmupLabel = stringResource(Res.string.workout_calendar_screen_finisher_label),
            finisherLabel = stringResource(Res.string.workout_calendar_screen_favourite_content_description),
            favoriteContentDescription = stringResource(Res.string.dashboard_fab_content_description),
        )
    }
}
