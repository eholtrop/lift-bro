package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
        title = { Text("Datasource Settings") },
    ) {
        val enabled = true

        Column {
            RadioField(
                text = "Local",
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
            RadioField(
                text = "Remote",
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
}
