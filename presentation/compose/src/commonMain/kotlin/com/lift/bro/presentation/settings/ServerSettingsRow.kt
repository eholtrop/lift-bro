package com.lift.bro.presentation.settings

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.ui.RadioField

@Composable
fun ServerSettingsRow(
    server: LiftBroServer
) {
    SettingsRowItem(
        modifier = Modifier,
        title = { Text("Server Settings") },
    ) {
        // Use server.isRunning() directly instead of local state to avoid sync issues
        val enabled = server.isRunning()

        RadioField(
            text = "Enable Server",
            selected = enabled,
            fieldSelected = {
                if (server.isRunning()) {
                    server.stop()
                } else {
                    server.start()
                }
                // Force recomposition by not storing state locally
            },
        )
    }
}
