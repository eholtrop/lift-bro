package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.lift.bro.BackupService
import com.lift.bro.BackupUseCase
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.settings_backup_cta
import lift_bro.core.generated.resources.settings_backup_restore_title
import lift_bro.core.generated.resources.settings_restore_cta
import org.jetbrains.compose.resources.stringResource

@Composable
fun BackupSettingsRow() {
    SettingsRowItem(
        title = { Text(stringResource(Res.string.settings_backup_restore_title)) }
    ) {
        val scope = rememberCoroutineScope()

        Row(
            modifier = Modifier.selectableGroup(),
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch {
                        BackupUseCase().invoke()
                    }
                }
            ) {
                Text(stringResource(Res.string.settings_backup_cta))
            }

            Space(MaterialTheme.spacing.one)

            Button(
                modifier = Modifier.weight(1f),
                onClick = {
                    scope.launch {
                        BackupService.restore()
                    }
                }
            ) {
                Text(stringResource(Res.string.settings_restore_cta))
            }
        }
    }
}
