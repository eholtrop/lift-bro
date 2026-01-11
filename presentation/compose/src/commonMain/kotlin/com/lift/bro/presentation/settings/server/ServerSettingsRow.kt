package com.lift.bro.presentation.settings.server

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.presentation.settings.SettingsRowItem
import com.lift.bro.ui.RadioField
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

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

    ServerSettingsRowContent(
        state = state,
        localIPAddress = getLocalIPAdderess(),
        onEvent = { interactor(it) }
    )
}

@Composable
fun ServerSettingsRowContent(
    state: ServerSettingsState,
    localIPAddress: String?,
    onEvent: (ServerSettingsEvent) -> Unit
) {
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
                        ServerStatus.On -> onEvent(ServerSettingsEvent.TurnOffServer)
                        ServerStatus.Unknown -> {}
                        ServerStatus.Off -> onEvent(ServerSettingsEvent.TurnOnServer)
                    }
                },
            )
            localIPAddress?.let {
                if (state.status == ServerStatus.On) {
                    Text("Current IP is $it")
                }
            }
        }
    }
}

@Preview
@Composable
fun ServerSettingsRowPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        ServerSettingsRowContent(
            state = ServerSettingsState(status = ServerStatus.On),
            localIPAddress = "192.168.1.100",
            onEvent = {}
        )
        ServerSettingsRowContent(
            state = ServerSettingsState(status = ServerStatus.Off),
            localIPAddress = null,
            onEvent = {}
        )
        ServerSettingsRowContent(
            state = ServerSettingsState(status = ServerStatus.Off),
            localIPAddress = "192.168.1.100",
            onEvent = {}
        )
    }
}
