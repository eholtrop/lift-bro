package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.lift.bro.di.dependencies
import com.lift.bro.server.LiftBroServerForegroundService

@Composable
actual fun ServerSettingsRow() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(LiftBroServerForegroundService.isEnabled(context)) }

    SettingsRowItem(
        title = { Text("Local HTTP Server") }
    ) {
        Row {
            Checkbox(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    if (it) {
                        dependencies.startPresentationServerService()
                    } else {
                        dependencies.stopPresentationServerService()
                    }
                    LiftBroServerForegroundService.saveEnabled(context, it)
                }
            )
            Text(if (enabled) "Enabled" else "Disabled")
        }
    }
}
