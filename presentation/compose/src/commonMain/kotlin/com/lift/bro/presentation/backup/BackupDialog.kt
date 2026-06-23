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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.lift.bro.ui.Space
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.compose.ShareResultLauncher
import io.github.vinceglb.filekit.dialogs.compose.rememberShareFileLauncher
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.backup_dialog_cancel_cta
import lift_bro.core.generated.resources.backup_dialog_title
import lift_bro.core.generated.resources.backup_progress_categories
import lift_bro.core.generated.resources.backup_progress_complete_description
import lift_bro.core.generated.resources.backup_progress_daily_notes
import lift_bro.core.generated.resources.backup_progress_movements
import lift_bro.core.generated.resources.backup_progress_sets
import lift_bro.core.generated.resources.backup_progress_workouts
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackupDialog(
    onDismissRequest: (ShareResultLauncher, PlatformFile?) -> Unit,
    interactor: BackupInteractor = rememberBackupInteractor(),
) {
    val state by interactor.state.collectAsState()

    val shareLauncher = rememberShareFileLauncher()
    LaunchedEffect(state.file) {
        state.file?.let { file ->
            onDismissRequest(shareLauncher, file)
        }
    }

    InfoDialog(
        onDismissRequest = { onDismissRequest(shareLauncher, null) },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
        title = {
            Text(
                text = stringResource(Res.string.backup_dialog_title),
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
        },
        confirmButtonText = stringResource(Res.string.backup_dialog_cancel_cta),
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
                title = stringResource(Res.string.backup_progress_categories)
            )
            BackupProgressRow(
                complete = variations != null,
                title = stringResource(Res.string.backup_progress_movements)
            )
            BackupProgressRow(
                complete = sets != null,
                title = stringResource(Res.string.backup_progress_sets)
            )
            BackupProgressRow(
                complete = workouts != null,
                title = stringResource(Res.string.backup_progress_workouts)
            )
            BackupProgressRow(
                complete = liftingLogs != null,
                title = stringResource(Res.string.backup_progress_daily_notes)
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
                contentDescription = stringResource(Res.string.backup_progress_complete_description),
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
