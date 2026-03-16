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
        title = { Text("Sorting") },
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
                        text = "Favourites at top"
                    )
                }

                HorizontalDivider()

                SortingOption.entries.forEach { option ->
                    RadioField(
                        text = option.name,
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
