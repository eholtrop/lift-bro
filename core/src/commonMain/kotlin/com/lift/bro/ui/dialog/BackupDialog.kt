package com.lift.bro.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.lift.bro.BackupService
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.BackupSettings
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.backup_dialog_message
import lift_bro.core.generated.resources.backup_dialog_primary_cta
import lift_bro.core.generated.resources.backup_dialog_secondary_cta
import lift_bro.core.generated.resources.backup_dialog_title
import org.jetbrains.compose.resources.stringResource


@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BackupDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
) {

    BasicAlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest
    ) {

    }

    val scope = rememberCoroutineScope()

    AlertDialog(
        modifier = Modifier,
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(Res.string.backup_dialog_title)) },
        text = { Text(stringResource(Res.string.backup_dialog_message)) },
        confirmButton = {
            Button(
                onClick = {
                    scope.launch {
                        BackupService.backup()
                        onDismissRequest()
                    }
                }
            ) {
                Text(stringResource(Res.string.backup_dialog_primary_cta))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    dependencies.settingsRepository.saveBackupSettings(
                        BackupSettings(
                            lastBackupDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
                        )
                    )
                    onDismissRequest()
                }
            ) {
                Text(stringResource(Res.string.backup_dialog_secondary_cta))
            }
        }
    )
}