package com.lift.bro.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.presentation.set.TempoState
import com.lift.bro.ui.FabProperties
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

data class TimerState(
    val startupTime: Int = 10,
    val tempo: List<Tempo> = emptyList(),
    val perSetRest: Int = 3,
)

sealed interface TimerEvent {

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    state: TimerState,
    onEvent: (TimerEvent) -> Unit,
) {
    LiftingScaffold(
        title = { Text("Timer") },
        fabProperties = FabProperties(
            fabIcon = Icons.Default.PlayArrow,
            fabClicked = {

            },
            contentDescription = "Start Timer",
        )
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier.wrapContentHeight(),
                contentPadding = PaddingValues(
                    all = MaterialTheme.spacing.one,
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
            ) {
                item {
                    TimerCard(
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        TextField(
                            value = state.startupTime.toString(),
                            onValueChange = {},
                            label = { Text("Startup Time Buffer") },
                            colors = TextFieldDefaults.transparentColors(),
                        )
                    }
                }

                item {
                    Icon(
                        modifier = Modifier.fillMaxWidth(),
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = ""
                    )
                }

                item {
                    var expanded by remember { mutableStateOf(state.tempo.any { t -> t != state.tempo.firstOrNull() }) }
                    Row(
                        modifier = Modifier.wrapContentHeight(),
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            if (!expanded) {
                                val tempo = state.tempo.first()
                                TempoSelector(
                                    tempo = TempoState(tempo.down, tempo.hold, tempo.down),
                                    tempoChanged = {},
                                    title = {},
                                    navCoordinator = null,
                                )
                                Space(MaterialTheme.spacing.half)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.half, Alignment.CenterHorizontally)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = ""
                                    )
                                    Button(
                                        onClick = {
                                            expanded = true
                                        },
                                        colors = ButtonDefaults.textButtonColors(),
                                    ) {
                                        Text("x${state.tempo.size}")
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowUpward,
                                        contentDescription = ""
                                    )
                                }
                                Space(MaterialTheme.spacing.half)
                                TimerCard(
                                    modifier = Modifier.fillMaxWidth(),
                                ) {
                                    TextField(
                                        value = state.perSetRest.toString(),
                                        onValueChange = {},
                                        label = { Text("Rest/Reset") },
                                        colors = TextFieldDefaults.transparentColors(),
                                    )
                                }
                            } else {
                                state.tempo.forEachIndexed { index, tempo ->
                                    TempoSelector(
                                        tempo = TempoState(tempo.down, tempo.hold, tempo.down),
                                        tempoChanged = {},
                                        title = {},
                                        navCoordinator = null,
                                    )
                                    if (index != state.tempo.lastIndex) {
                                        Space(MaterialTheme.spacing.quarter)
                                        TimerCard(
                                            modifier = Modifier.fillMaxWidth(),
                                        ) {
                                            TextField(
                                                value = state.startupTime.toString(),
                                                onValueChange = {},
                                                label = { Text("Rest") },
                                                colors = TextFieldDefaults.transparentColors(),
                                            )
                                        }
                                        Space(MaterialTheme.spacing.half)
                                        Icon(
                                            modifier = Modifier.fillMaxWidth(),
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = "",
                                        )
                                        Space(MaterialTheme.spacing.half)
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "",
                        )
                        Space(MaterialTheme.spacing.one)
                        Text(
                            text = "\uD83D\uDE2E\u200D\uD83D\uDCA8",
                            style = MaterialTheme.typography.headlineLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimerCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surface,
            shape = MaterialTheme.shapes.medium,
        )
    ) {
        content()
    }
}


@Preview
@Composable
fun TimerScreenPreview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState(
                tempo = listOf(Tempo(), Tempo(), Tempo())
            ),
            onEvent = {}
        )
    }
}

@Preview
@Composable
fun TimerScreen_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState(
                tempo = listOf(Tempo(down = 10), Tempo(), Tempo())
            ),
            onEvent = {}
        )
    }
}
