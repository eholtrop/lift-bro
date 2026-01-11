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
                    Text("Ok")
                }
            },
            title = {
                Text("What do these mean?")
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
                text = "With a tempo of...",
                style = MaterialTheme.typography.titleMedium
            )
            InfoDialogButton(
                dialogTitle = { Text("What is Tempo?") },
                dialogMessage = { InfoDialogText() }
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
        ) {
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Ecc",
                selectedNum = down,
                numberChanged = downChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Iso",
                selectedNum = hold,
                numberChanged = holdChanged
            )
            NumberPicker(
                modifier = Modifier.weight(.33f).height(52.dp),
                title = "Con",
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
            text = "Eccentric - Ecc",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Muscle Extension",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = "• putting a deadlift down\n• going into a squat\n• lowering the bar towards your chest in a bench press",
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = "Isometric - Iso",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Between Eccentric and Concentric",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = "• a deadlift/squad/bench press hold\n• time under tension",
            style = MaterialTheme.typography.bodyMedium,
        )
        Space(MaterialTheme.spacing.one)
        Text(
            text = "Concentric - Con",
            style = MaterialTheme.typography.titleMedium,
        )
        Text(
            text = "Muscle Contraction",
            style = MaterialTheme.typography.labelMedium,
        )
        Text(
            modifier = Modifier.padding(start = MaterialTheme.spacing.half),
            text = "• picking up a deadlift\n• coming up from a squat\n• pushing the bar up in a bench press",
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
