package com.lift.bro.presentation.settings.server

import androidx.compose.runtime.Composable
import com.lift.bro.domain.server.LiftBroServer
import com.lift.bro.mvi.Interactor
import com.lift.bro.mvi.Reducer
import com.lift.bro.mvi.SideEffect
import com.lift.bro.mvi.compose.rememberInteractor
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.serialization.Serializable

typealias ServerSettingsInteractor = Interactor<ServerSettingsState, ServerSettingsEvent>

@Serializable
data class ServerSettingsState(
    val status: ServerStatus = ServerStatus.Unknown,
)

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
                ServerSettingsEvent.TurnOffServer, ServerSettingsEvent.TurnOnServer -> state.copy(
                    status = ServerStatus.Unknown
                )
                is ServerSettingsEvent.ServerStatusUpdated -> state.copy(status = event.status)
            }
        }
    ),
    sideEffects = listOf(
        SideEffect { dispatcher, _, event ->
            when (event) {
                ServerSettingsEvent.TurnOffServer -> {
                    server.stop()
                    while (server.isRunning()) {
                        delay(500)
                    }
                    dispatcher(ServerSettingsEvent.ServerStatusUpdated(ServerStatus.Off))
                }
                ServerSettingsEvent.TurnOnServer -> {
                    server.start()
                    withTimeoutOrNull(2000L) {
                        dispatcher(ServerSettingsEvent.ServerStatusUpdated(ServerStatus.On))
                    }
                    while (!server.isRunning()) {
                        delay(500)
                    }
                }
                is ServerSettingsEvent.ServerStatusUpdated -> {}
            }
        }
    )
)
