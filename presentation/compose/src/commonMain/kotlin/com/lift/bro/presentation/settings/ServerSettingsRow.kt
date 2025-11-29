package com.lift.bro.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.LocalSubscriptionStatusProvider
import com.lift.bro.ui.RadioField

expect fun getLocalIPAdderess() : String?


@Composable
fun ServerSettingsRow(
    server: LiftBroServer,
) {
    SettingsRowItem(
        modifier = Modifier,
        title = { Text("Datasource Settings") },
    ) {
        var enabled by remember { mutableStateOf(server.isRunning()) }

        Column {
            Text("Enable Local Server")
            Text("Enabling this will run a local Lift Bro server on this device! Allowing other instances of lift bro to use this current phone as its source of truth! (ex: connect to this lift bro from a tablet for more screen real estate")
            RadioField(
                text = "Server Enabled",
                selected = enabled,
                fieldSelected = {
                    if (server.isRunning()) {
                        server.stop()
                        enabled = false
                    } else {
                        server.start()
                        enabled = true
                    }
                },
            )

            getLocalIPAdderess()?.let {
                if (LocalServer.current?.isRunning() == true) {
                    Text("Current IP is $it")
                }
            }

            var serverUrl by remember { mutableStateOf("") }

            Text("Connect to Another Server")
            Text("Connect to another Lift Bro Instance, This will not impact your currently saved data on this device")
            TextField(
                value = serverUrl,
                onValueChange = { serverUrl = it },
                placeholder = { Text("http://192.168.0.0:8080") },
            )

            Button(
                onClick = {

                }
            ) {
                Text("Connect")
            }

        }
    }
}
