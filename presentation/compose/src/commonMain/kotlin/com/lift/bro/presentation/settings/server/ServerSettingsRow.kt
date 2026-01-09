package com.lift.bro.presentation.settings.server

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.settings.SettingsRowItem
import com.lift.bro.ui.RadioField
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d

expect fun getLocalIPAdderess(): String?

enum class ServerStatus {
    On, Unknown, Off
}

@Composable
fun ServerSettingsRow(
    server: LiftBroServer,
    interactor: ServerSettingsInteractor = rememberServerSettingsInteractor(server)
) {
    val state by interactor.state.collectAsState()

    SettingsRowItem(
        modifier = Modifier,
        title = { Text("Lift Bro Local Server") },
    ) {
        Column {
            Text(
                "Enabling this will run a Lift Bro server on this device. It will expose all lift bro data to anyone on your current network"
            )
            RadioField(
                text = "Server ${if (state.status == ServerStatus.On) "Enabled" else "Disabled"}",
                selected = state.status == ServerStatus.On,
                fieldSelected = {
                    when (state.status) {
                        ServerStatus.On -> interactor(ServerSettingsEvent.TurnOffServer)
                        ServerStatus.Unknown -> {}
                        ServerStatus.Off -> interactor(ServerSettingsEvent.TurnOnServer)
                    }
                },
            )
            getLocalIPAdderess()?.let {
                if (LocalServer.current?.isRunning() == true) {
                    Text("Current IP is $it")
                }
            }
        }
    }
}
