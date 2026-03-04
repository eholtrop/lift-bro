package com.lift.bro.presentation.set

import androidx.compose.runtime.Composable
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.create_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_delete_acc_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_label
import lift_bro.core.generated.resources.edit_set_screen_extra_notes_placeholder
import lift_bro.core.generated.resources.edit_set_screen_title
import lift_bro.core.generated.resources.edit_set_screen_variation_selector_empty_state_title
import lift_bro.core.generated.resources.tempo_selector_timer_content_description
import org.jetbrains.compose.resources.stringResource

data class EditSetScreenStrings(
    val createSetTitle: String,
    val editSetTitle: String,
    val deleteContentDescription: String,
    val extraNotesLabel: String,
    val extraNotesPlaceholder: String,
    val variationSelectorEmptyState: String,
    val timerContentDescription: String,
) {
    companion object {
        @Composable
        fun default(): EditSetScreenStrings = EditSetScreenStrings(
            createSetTitle = stringResource(Res.string.create_set_screen_title),
            editSetTitle = stringResource(Res.string.edit_set_screen_title),
            deleteContentDescription = stringResource(Res.string.edit_set_screen_delete_acc_label),
            extraNotesLabel = stringResource(Res.string.edit_set_screen_extra_notes_label),
            extraNotesPlaceholder = stringResource(Res.string.edit_set_screen_extra_notes_placeholder),
            variationSelectorEmptyState = stringResource(
                Res.string.edit_set_screen_variation_selector_empty_state_title
            ),
            timerContentDescription = stringResource(Res.string.tempo_selector_timer_content_description),
        )
    }
}
