package com.lift.bro.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lift.bro.BackupUseCase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.utils.toLocalDate
import com.lift.bro.`ktx-datetime`.today
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.todayIn
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.backup_dialog_message
import lift_bro.core.generated.resources.backup_dialog_primary_cta
import lift_bro.core.generated.resources.backup_dialog_secondary_cta
import lift_bro.core.generated.resources.backup_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackupAlertDialog(
    modifier: Modifier = Modifier,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
) {
    var showBackupModal by remember { mutableStateOf(false) }
    LaunchedEffect("force_backup_prompt") {
        val lastBackupDate = dependencies.settingsRepository.getBackupSettings().first().lastBackupDate
        val today = Clock.System.today

        // if the user has not backed up before
        val comparisonDate = if (lastBackupDate.toEpochDays() == 0) {
            val sets = dependencies.database.setDataSource.getAll()
            // use the earliest set as the reference date, if there are none just use the current day
            sets.minOfOrNull { it.date }?.toLocalDate() ?: today
        } else {
            // use the last backup date
            lastBackupDate
        }

        // check if the comparison date was more than a week ago
        showBackupModal = comparisonDate.daysUntil(today) >= 7
    }

    if (showBackupModal) {
        AlertDialog(
            modifier = modifier,
            onDismissRequest = {
                showBackupModal = false
            },
            title = { Text(stringResource(Res.string.backup_dialog_title)) },
            text = { Text(stringResource(Res.string.backup_dialog_message)) },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            BackupUseCase().invoke()
                            showBackupModal = false
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
                        showBackupModal = false
                    }
                ) {
                    Text(stringResource(Res.string.backup_dialog_secondary_cta))
                }
            }
        )
    }
}
