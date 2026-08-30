package com.lift.bro.ui.dialog

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.restore_confirm_dialog_confirm_cta
import lift_bro.core.generated.resources.restore_confirm_dialog_dismiss_cta
import lift_bro.core.generated.resources.restore_confirm_dialog_message
import lift_bro.core.generated.resources.restore_confirm_dialog_title
import org.jetbrains.compose.resources.stringResource

@Composable
fun RestoreConfirmDialog(
    modifier: Modifier = Modifier,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.restore_confirm_dialog_title)) },
        text = { Text(stringResource(Res.string.restore_confirm_dialog_message)) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(Res.string.restore_confirm_dialog_confirm_cta))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(Res.string.restore_confirm_dialog_dismiss_cta))
            }
        },
    )
}
