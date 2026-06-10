package com.lift.bro.presentation.backup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme

@Composable
fun BackupDialog(
    onDismissRequest: () -> Unit,
    interactor: BackupInteractor = rememberBackupInteractor(onDismissRequest),
) {
    val state by interactor.state.collectAsStateWithLifecycle()

    InfoDialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        confirmButtonText = "Cancel",
        title = {
            Text(
                text = "Backing up Lift Bro",
                style = MaterialTheme.typography.headlineLarge
            )
        },
        message = {
            LaunchedEffect(state.backupFinished) {
                if (state.backupFinished) {
                    interactor(BackupEvent.BackupFinished)
                }
            }
            BackupDialogContent(state)
        }
    )
}

@Composable
private fun BackupDialogContent(
    state: BackupState,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half)
    ) {
        with(state.backup) {
            BackupProgressRow(
                complete = lifts != null,
                title = "Categories"
            )
            BackupProgressRow(
                complete = variations != null,
                title = "Movements"
            )
            BackupProgressRow(
                complete = sets != null,
                title = "Sets"
            )
            BackupProgressRow(
                complete = workouts != null,
                title = "Workouts"
            )
            BackupProgressRow(
                complete = liftingLogs != null,
                title = "Daily Notes"
            )
        }
    }
}

@Composable
private fun BackupProgressRow(
    modifier: Modifier = Modifier,
    complete: Boolean,
    title: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (complete) {
            true -> Icon(
                modifier = Modifier.size(MaterialTheme.spacing.oneAndHalf),
                imageVector = Icons.Default.Check,
                contentDescription = "complete",
                tint = MaterialTheme.colorScheme.onPrimary
            )

            false -> CircularProgressIndicator(
                modifier = Modifier.size(MaterialTheme.spacing.oneAndHalf),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.onPrimary,
            )
        }

        Space(MaterialTheme.spacing.one)

        Text(
            title,
            style = MaterialTheme.typography.titleLarge,
            color = when (complete) {
                true -> MaterialTheme.colorScheme.onPrimary
                false -> MaterialTheme.colorScheme.surfaceDim
            }
        )
    }
}

@Preview
@Composable
fun BackupDialogDarkPreview(
    @PreviewParameter(BackupStateProvider::class) state: BackupState,
) {
    PreviewAppTheme(true) {
        BackupDialogContent(state)
    }
}

@Preview
@Composable
fun BackupDialogLightPreview(
    @PreviewParameter(BackupStateProvider::class) state: BackupState,
) {
    PreviewAppTheme(false) {
        BackupDialogContent(state)
    }
}
