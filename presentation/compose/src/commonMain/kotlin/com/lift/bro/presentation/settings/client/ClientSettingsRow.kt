package com.lift.bro.presentation.settings.client

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.lift.bro.data.client.datasources.KtorServerHealthDataSource
import com.lift.bro.presentation.settings.SettingsRowItem
import com.lift.bro.ui.Space
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.coroutines.launch
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.client_settings_apply
import lift_bro.core.generated.resources.client_settings_local_description
import lift_bro.core.generated.resources.client_settings_mode_local
import lift_bro.core.generated.resources.client_settings_mode_remote
import lift_bro.core.generated.resources.client_settings_row_title
import lift_bro.core.generated.resources.client_settings_server_url_label
import lift_bro.core.generated.resources.client_settings_server_url_placeholder
import lift_bro.core.generated.resources.client_settings_test_connection
import org.jetbrains.compose.resources.stringResource

@Composable
fun ClientSettingsRow(
    interactor: ClientSettingsInteractor = rememberClientSettingsInteractor(),
) {
    val state by interactor.state.collectAsState()

    ClientSettingsRowContent(
        state = state,
        onEvent = { interactor(it) }
    )
}

@Composable
fun ClientSettingsRowContent(
    state: ClientSettingsState,
    onEvent: (ClientSettingsEvent) -> Unit
) {
    SettingsRowItem(
        modifier = Modifier,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(stringResource(Res.string.client_settings_row_title))
                Space()
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(
                        onClick = {
                            expanded = true
                        },
                        colors = ButtonDefaults.textButtonColors()
                    ) {
                        Text(
                            when (state.mode) {
                                ClientMode.Local -> stringResource(Res.string.client_settings_mode_local)
                                is ClientMode.Remote -> stringResource(Res.string.client_settings_mode_remote)
                            },
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = {
                            expanded = false
                        }
                    ) {
                        state.options.forEach { location ->
                            DropdownMenuItem(
                                text = {
                                    Text(location.toString())
                                },
                                onClick = {
                                    onEvent(ClientSettingsEvent.ClientModeSelected(location))
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
    ) {
        Column(
            modifier = Modifier.animateContentSize(),
        ) {
            var connected by remember(state.mode) { mutableStateOf(state.mode is ClientMode.Local) }

            Crossfade(state.mode) { mode ->
                when (mode) {
                    ClientMode.Local -> {
                        Text(stringResource(Res.string.client_settings_local_description))
                    }

                    is ClientMode.Remote -> {
                        Column {
                            var value by remember(
                                state.mode::class
                            ) { mutableStateOf((state.mode as? ClientMode.Remote)?.url) }
                            TextField(
                                modifier = Modifier.fillMaxWidth(),
                                value = value ?: "",
                                onValueChange = {
                                    value = it
                                    onEvent(ClientSettingsEvent.UrlUpdated(it))
                                },
                                placeholder = {
                                    Text(stringResource(Res.string.client_settings_server_url_placeholder))
                                },
                                label = { Text(stringResource(Res.string.client_settings_server_url_label)) }
                            )

                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.End
                            ) {
                                val scope = rememberCoroutineScope()
                                Button(
                                    onClick = {
                                        scope.launch {
                                            connected = KtorServerHealthDataSource(url = mode.url).check()
                                        }
                                    },
                                    colors = ButtonDefaults.filledTonalButtonColors()
                                ) {
                                    Text(stringResource(Res.string.client_settings_test_connection))
                                }
                            }
                        }
                    }
                }
            }

            Button(
                modifier = Modifier.align(Alignment.End),
                onClick = {
                    onEvent(ClientSettingsEvent.ApplyMode(state.mode))
                },
                enabled = connected
            ) {
                Text(stringResource(Res.string.client_settings_apply))
            }
        }
    }
}

@Preview
@Composable
fun ClientSettingsRowLocalPreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        ClientSettingsRowContent(
            state = ClientSettingsState(mode = ClientMode.Local),
            onEvent = {}
        )
    }
}

@Preview
@Composable
fun ClientSettingsRowRemotePreview(
    @PreviewParameter(DarkModeProvider::class) darkMode: Boolean
) {
    PreviewAppTheme(isDarkMode = darkMode) {
        ClientSettingsRowContent(
            state = ClientSettingsState(mode = ClientMode.Remote("http://192.168.1.100:8080")),
            onEvent = {}
        )
    }
}
