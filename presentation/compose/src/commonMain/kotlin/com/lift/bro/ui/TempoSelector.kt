package com.lift.bro.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.dialog.InfoDialogButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.tempo_selector_dialog_ok_cta
import lift_bro.core.generated.resources.tempo_selector_dialog_what_mean_title
import lift_bro.core.generated.resources.tempo_selector_with_tempo_text
import lift_bro.core.generated.resources.tempo_selector_dialog_title
import lift_bro.core.generated.resources.tempo_selector_ecc_title
import lift_bro.core.generated.resources.tempo_selector_iso_title
import lift_bro.core.generated.resources.tempo_selector_con_title
import lift_bro.core.generated.resources.tempo_selector_eccentric_title
import lift_bro.core.generated.resources.tempo_selector_eccentric_subtitle
import lift_bro.core.generated.resources.tempo_selector_eccentric_examples
import lift_bro.core.generated.resources.tempo_selector_isometric_title
import lift_bro.core.generated.resources.tempo_selector_isometric_subtitle
import lift_bro.core.generated.resources.tempo_selector_isometric_examples
import lift_bro.core.generated.resources.tempo_selector_concentric_title
import lift_bro.core.generated.resources.tempo_selector_concentric_subtitle
import lift_bro.core.generated.resources.tempo_selector_concentric_examples
import org.jetbrains.compose.resources.stringResource

@Composable
fun TempoSelector(
    modifier: Modifier = Modifier,
    down: Int?,
    hold: Int?,
    up: Int?,
    downChanged: (Int?) -> Unit,
    holdChanged: (Int?) -> Unit,
    upChanged: (Int?) -> Unit,
) {
    var showInfoModal by remember { mutableStateOf(false) }

    if (showInfoModal) {
        AlertDialog(
            onDismissRequest = { showInfoModal = false },
            confirmButton = {
                Button(
                    onClick = { showInfoModal = false }
                ) {
                    Text(stringResource(Res.string.tempo_selector_dialog_ok_cta))
                }
            },
            title = {
                Text(stringResource(Res.string.tempo_selector_dialog_what_mean_title))
            },
            text = {
                InfoDialogText()
            }
        )
    }

    Column(
        modifier = modifier,
//        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(Res.string.tempo_selector_with_tempo_text),
                style = MaterialTheme.typography.titleMedium
            )
            InfoDialogButton(
                dialogTitle = { Text(stringResource(Res.string.tempo_selector_dialog_title)) },
                dialogMessage = { InfoDialogText() }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_ecc_title),
                selectedNum = down,
                numberChanged = downChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_iso_title),
                selectedNum = hold,
                numberChanged = holdChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = stringResource(Res.string.tempo_selector_con_title),
                selectedNum = up,
                numberChanged = upChanged,
                imeAction = ImeAction.Done
            )
        }
    }
}

@Composable
private fun InfoDialogText() {
    Column {
        Text(
            text = stringResource(Res.string.tempo_selector_eccentric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_eccentric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_eccentric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = stringResource(Res.string.tempo_selector_isometric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_isometric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_isometric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = stringResource(Res.string.tempo_selector_concentric_title),
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = stringResource(Res.string.tempo_selector_concentric_subtitle),
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = stringResource(Res.string.tempo_selector_concentric_examples),
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Preview
@Composable
fun TempoSelectorPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            // With all values
            TempoSelector(
                down = 3,
                hold = 1,
                up = 1,
                downChanged = {},
                holdChanged = {},
                upChanged = {}
            )

            // With some null values
            TempoSelector(
                down = 2,
                hold = null,
                up = 1,
                downChanged = {},
                holdChanged = {},
                upChanged = {}
            )
        }
    }
}
