package com.lift.bro.presentation.settings.server

import androidx.compose.runtime.Composable
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.presentation.Interactor
import com.lift.bro.presentation.Reducer
import com.lift.bro.presentation.rememberInteractor
import com.lift.bro.utils.debug
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable

typealias ServerSettingsInteractor = Interactor<ServerSettingsState, ServerSettingsEvent>


@Serializable
data class ServerSettingsState(
    val status: ServerStatus = ServerStatus.Unknown,
) {
}

sealed interface ServerSettingsEvent {
    data object TurnOnServer: ServerSettingsEvent
    data object TurnOffServer: ServerSettingsEvent

    data class ServerStatusUpdated(val status: ServerStatus): ServerSettingsEvent
}

@Composable
fun rememberServerSettingsInteractor(
    server: LiftBroServer,
): ServerSettingsInteractor = rememberInteractor(
    initialState = ServerSettingsState(if (server.isRunning()) ServerStatus.On else ServerStatus.Off),
    source = {
        MutableStateFlow(it)
    },
    reducers = listOf(
        Reducer { state, event ->
            when (event) {
                ServerSettingsEvent.TurnOffServer, ServerSettingsEvent.TurnOnServer -> state.copy(status = ServerStatus.Unknown)
                is ServerSettingsEvent.ServerStatusUpdated -> state.copy(status = event.status)
            }
        }
    ),
    sideEffects = listOf(
        { state, event ->
            when (event) {
                ServerSettingsEvent.TurnOffServer -> {
                    server.stop()
                    while(server.isRunning()) {
                        delay(500)
                    }
                    invoke(ServerSettingsEvent.ServerStatusUpdated(ServerStatus.Off))
                }
                ServerSettingsEvent.TurnOnServer -> {
                    server.start()
                    while (!server.isRunning()) {
                        delay(500)
                    }
                    invoke(ServerSettingsEvent.ServerStatusUpdated(ServerStatus.On))
                }
                is ServerSettingsEvent.ServerStatusUpdated -> {}
            }
        }
    )
)
