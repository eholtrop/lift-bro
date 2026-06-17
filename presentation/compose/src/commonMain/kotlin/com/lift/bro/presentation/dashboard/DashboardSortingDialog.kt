package com.lift.bro.presentation.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.ui.RadioField
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.dashboard_sorting_dialog_title
import lift_bro.core.generated.resources.dashboard_sorting_favourites_at_top_text
import lift_bro.core.generated.resources.dashboard_sorting_option_heaviest
import lift_bro.core.generated.resources.dashboard_sorting_option_latest
import lift_bro.core.generated.resources.dashboard_sorting_option_name
import lift_bro.core.generated.resources.dashboard_sorting_option_reps
import org.jetbrains.compose.resources.stringResource

@Composable
fun DashboardSortingDialog(
    sortingSettings: SortingSettings = SortingSettings(),
    onDismissRequest: () -> Unit,
    optionSelected: (SortingOption) -> Unit,
    toggleFavourite: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {},
        title = { Text(stringResource(Res.string.dashboard_sorting_dialog_title)) },
        text = {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = sortingSettings.favouritesAtTop,
                        onCheckedChange = { toggleFavourite() }
                    )
                    Text(
                        text = stringResource(Res.string.dashboard_sorting_favourites_at_top_text)
                    )
                }

                HorizontalDivider()

                SortingOption.entries.forEach { option ->
                    RadioField(
                        text = when (option) {
                            SortingOption.Heaviest -> stringResource(Res.string.dashboard_sorting_option_heaviest)
                            SortingOption.Reps -> stringResource(Res.string.dashboard_sorting_option_reps)
                            SortingOption.Latest -> stringResource(Res.string.dashboard_sorting_option_latest)
                            SortingOption.Name -> stringResource(Res.string.dashboard_sorting_option_name)
                        },
                        selected = sortingSettings.option == option
                    ) {
                        optionSelected(option)
                    }
                }
            }
        }
    )
}

@Preview
@Composable
fun DashboardSortingDialogPreview(@PreviewParameter(DarkModeProvider::class) dark: Boolean) {
    PreviewAppTheme(
        isDarkMode = dark
    ) {
        DashboardSortingDialog(
            optionSelected = {},
            toggleFavourite = {},
            onDismissRequest = {}
        )
    }
}
