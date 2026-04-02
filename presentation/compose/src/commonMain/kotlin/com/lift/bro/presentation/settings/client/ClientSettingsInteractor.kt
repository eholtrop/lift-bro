package com.lift.bro.presentation.settings.client

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.settings.client.ClientMode.Local
import com.lift.bro.presentation.settings.client.ClientMode.Remote
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor

typealias ClientSettingsInteractor = Interactor<ClientSettingsState, ClientSettingsEvent>

@Serializable
sealed interface ClientMode {

    @Serializable
    data object Local: ClientMode

    @Serializable
    data class Remote(
        val url: String = "http://localhost:8080",
    ): ClientMode
}

// @Serializable
// sealed class RemoteProtocol {
//    @Serializable
//    data object Http: RemoteProtocol()
//    @Serializable
//    data object Https: RemoteProtocol()
//    @Serializable
//    data class Custom(val protocol: String): RemoteProtocol()
// }

enum class ClientModeOptions {
    Local, Remote
}

@Serializable
data class ClientSettingsState(
    val mode: ClientMode = ClientMode.Local,
) {
    val options: List<ClientModeOptions> = ClientModeOptions.entries.toList()
}

sealed interface ClientSettingsEvent {
    data class ClientModeSelected(val option: ClientModeOptions): ClientSettingsEvent

    data class UrlUpdated(val url: String): ClientSettingsEvent

    data class ApplyMode(val mode: ClientMode): ClientSettingsEvent
}

@Composable
fun rememberClientSettingsInteractor(): ClientSettingsInteractor = rememberInteractor(
    initialState = ClientSettingsState(),
    source = { state ->
        flow {
            emit(
                ClientSettingsState(mode = dependencies.settingsRepository.getClientUrl()?.let { Remote(it) } ?: Local)
            )
        }
    },
    reducers = listOf(
        Reducer { state, event ->
            when (event) {
                is ClientSettingsEvent.ClientModeSelected -> state.copy(
                    mode = when (event.option) {
                        ClientModeOptions.Local -> Local
                        ClientModeOptions.Remote -> Remote()
                    }
                )

                is ClientSettingsEvent.UrlUpdated -> state.copy(
                    mode = Remote(event.url)
                )

                is ClientSettingsEvent.ApplyMode -> state
            }
        }
    ),
    sideEffects = listOf(
        SideEffect { _, _, event ->
            when (event) {
                is ClientSettingsEvent.ClientModeSelected -> {}
                is ClientSettingsEvent.UrlUpdated -> {}
                is ClientSettingsEvent.ApplyMode -> {
                    when (val mode = event.mode) {
                        Local -> dependencies.settingsRepository.setClientUrl(null)
                        is Remote -> dependencies.settingsRepository.setClientUrl(mode.url)
                    }
                }
            }
        }
    )
)
