package com.lift.bro.presentation.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.camera.CameraController
import com.lift.bro.presentation.camera.CameraPreview
import com.lift.bro.presentation.camera.rememberCameraControllerFactory
import com.lift.bro.presentation.camera.rememberCameraPermissionHandler
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.card.lift.weightFormat
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import tv.dpal.compose.isOpen

@Composable
fun TimerScreen(
    reps: Int,
    tempo: Tempo,
) {
    val interactor: TimerInteractor = rememberTimerInteractor(reps, tempo)

    TimerScreen(
        state = interactor.state.collectAsState().value,
        onEvent = interactor::invoke,
    )
}

@Composable
fun TimerScreen(
    setId: String,
) {
    val interactor: TimerInteractor = rememberTimerInteractor(setId)

    TimerScreen(
        state = interactor.state.collectAsState().value,
        onEvent = interactor::invoke,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    state: TimerState,
    onEvent: (TimerEvent) -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
    ) {
        var cameraController by remember { mutableStateOf<CameraController?>(null) }
        val cameraControllerFactory = rememberCameraControllerFactory()

        if (cameraController != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                CameraPreview(
                    controller = cameraController!!,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        LiftingScaffold(
            title = { },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            fab = {
                AnimatedVisibility(
                    visible = !LocalSoftwareKeyboardController.current.isOpen(),
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when (state) {
                            is TimerState.Ended -> {
                                TimerTrack(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                    scrollable = true,
                                    segments = state.timers
                                )
                            }

                            is TimerState.Plan -> {
                                val permissionHandler = rememberCameraPermissionHandler()

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    IconButton(
                                        onClick = {
                                            onEvent(TimerEvent.ToggleAudio)
                                        },
                                    ) {
                                        if (state.audio) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Default.VolumeUp,
                                                contentDescription = "Mute Sound"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Default.VolumeOff,
                                                contentDescription = "Play Sound"
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = {
                                            if (state.cameraEnabled) {
                                                cameraController?.release()
                                                cameraController = null
                                                onEvent(TimerEvent.ToggleCamera)
                                            } else {
                                                if (permissionHandler.isGranted) {
                                                    cameraController = cameraControllerFactory.create()
                                                    onEvent(TimerEvent.ToggleCamera)
                                                } else {
                                                    permissionHandler.requestPermission { granted ->
                                                        if (granted) {
                                                            cameraController = cameraControllerFactory.create()
                                                            onEvent(TimerEvent.ToggleCamera)
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                    ) {
                                        if (state.cameraEnabled) {
                                            Icon(
                                                imageVector = Icons.Default.Videocam,
                                                contentDescription = "Disable Camera"
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.VideocamOff,
                                                contentDescription = "Enable Camera"
                                            )
                                        }
                                    }
                                }

                                TimerTrack(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                    segments = state.runningTimer.timers,
                                    scrollable = true,
                                )
                            }

                            is TimerState.Running -> {
                                IconButton(
                                    modifier = Modifier.align(Alignment.Start),
                                    onClick = {
                                        onEvent(TimerEvent.ToggleAudio)
                                    },
                                ) {
                                    if (state.audio) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.VolumeUp,
                                            contentDescription = "Mute Sound"
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Default.VolumeOff,
                                            contentDescription = "Play Sound"
                                        )
                                    }
                                }
                                TimerTrack(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                    segments = state.timers,
                                    scrollable = state.paused
                                )
                            }
                        }
                        Row {
                            Button(
                                modifier = Modifier.defaultMinSize(52.dp, 52.dp),
                                onClick = {
                                    when (state) {
                                        is TimerState.Plan -> onEvent(TimerEvent.Plan.Start)
                                        is TimerState.Running -> if (state.paused) {
                                            onEvent(
                                                TimerEvent.Running.Resume
                                            )
                                        } else {
                                            onEvent(TimerEvent.Running.Pause)
                                        }

                                        is TimerState.Ended -> onEvent(TimerEvent.Ended.Restart)
                                    }
                                },
                                shape = when {
                                    else -> ButtonDefaults.shape
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    Icon(
                                        imageVector = when (state) {
                                            is TimerState.Plan -> Icons.Default.PlayArrow
                                            is TimerState.Running -> if (state.paused) Icons.Default.PlayArrow else Icons.Default.Pause
                                            is TimerState.Ended -> Icons.Default.Repeat
                                        },
                                        contentDescription = when (state) {
                                            is TimerState.Ended -> "Restart"
                                            is TimerState.Plan -> "Start"
                                            is TimerState.Running -> if (state.paused) "Resume" else "Pause"
                                        },
                                    )
                                }
                            }
                        }
                    }
                }
            },
        ) { padding ->
            TimerOverlay(
                modifier = Modifier.padding(padding),
                state = state,
                onEvent = onEvent,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun TimerOverlay(
    modifier: Modifier = Modifier,
    state: TimerState,
    onEvent: (TimerEvent) -> Unit,
) {
    Crossfade(
        targetState = state::class,
    ) {
        when (state) {
            is TimerState.Plan -> PlanTimerOverlay(
                modifier = modifier,
                state = state,
                onEvent = onEvent,
            )

            is TimerState.Running -> RunningTimerContent(
                modifier = modifier,
                state = state,
            )

            is TimerState.Ended -> {
                val twmEnabled = LocalTwmSettings.current
                val merEnabled = LocalShowMERCalcs.current?.enabled == true
                LazyColumn(
                    modifier = modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
                ) {
                    item {
                        Text(
                            "Great job!",
                            style = MaterialTheme.typography.displayMedium
                        )
                    }

                    state.set?.let { set ->
                        item {
                            Text(
                                "${set.reps} x ${weightFormat(set.weight)}! \uD83D\uDCAA",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(MaterialTheme.spacing.one))
                        }

                        if (twmEnabled || merEnabled) {
                            item {
                                InfoSpeechBubble(
                                    modifier = Modifier.fillMaxWidth(.6f),
                                    title = {},
                                    message = {
                                        if (twmEnabled) {
                                            Text(
                                                text = "TWM: ${weightFormat(set.totalWeightMoved)}"
                                            )
                                        }
                                        if (merEnabled) {
                                            Text(
                                                text = "+ ${set.mer} mers"
                                            )
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun textModifier() = Modifier.defaultMinSize(minWidth = 36.dp)
    .background(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = MaterialTheme.shapes.small,
    )
    .border(
        width = Dp.Hairline,
        color = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.small,
    )

@Composable
fun TimerSetField(
    modifier: Modifier = Modifier,
    rep: Int?,
    state: Tempo,
    rest: Long,
    onEvent: (TimerEvent) -> Unit,
    style: TextStyle = MaterialTheme.typography.displaySmall,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
    ) {
        TimerTextField(
            value = state.down.toString(),
            onValueChange = {
                it.toLongOrNull()?.let {
                    onEvent(TimerEvent.Plan.TempoChanged(rep = rep, tempo = state.copy(down = it)))
                }
            },
            label = { Text("Ecc") },
            textStyle = style,
        )
        TimerTextField(
            value = state.hold.toString(),
            onValueChange = {
                it.toLongOrNull()?.let {
                    onEvent(TimerEvent.Plan.TempoChanged(rep = rep, tempo = state.copy(hold = it)))
                }
            },
            label = { Text("Hold") },
            textStyle = style,
        )
        TimerTextField(
            value = state.up.toString(),
            onValueChange = {
                it.toLongOrNull()?.let {
                    onEvent(TimerEvent.Plan.TempoChanged(rep = rep, tempo = state.copy(up = it)))
                }
            },
            label = { Text("Con") },
            textStyle = style,
        )
        TimerTextField(
            value = rest.toString(),
            onValueChange = {
                it.toLongOrNull()?.let {
                    onEvent(TimerEvent.Plan.PerSetRestChanged(it))
                }
            },
            label = { Text("Rest") },
            textStyle = style,
        )
    }
}

@Composable
fun TimerTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: @Composable () -> Unit,
    suffix: @Composable (() -> Unit)? = null,
    textStyle: TextStyle = MaterialTheme.typography.displayMedium,
) {
    var thisValue by remember(value) { mutableStateOf(value) }
    TextField(
        modifier = textModifier().width(72.dp),
        value = thisValue,
        onValueChange = {
            onValueChange(it)
            thisValue = it
        },
        textStyle = textStyle.copy(textAlign = TextAlign.Center),
        label = label,
        suffix = suffix,
        colors = TextFieldDefaults.transparentColors(),
    )
}

@Preview
@Composable
fun TimerScreenPreview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Plan(
                tempo = listOf(Tempo(), Tempo(), Tempo())
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
fun TimerScreen_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Plan(
                tempo = listOf(Tempo(down = 10), Tempo(), Tempo())
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
fun TimerEndedScreen_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Ended(
                timers = listOf(
                    TimerSegment(
                        name = "Setup",
                        elapsedTime = 0,
                        totalTime = 5000,
                    ),
                    TimerSegment(
                        name = "Ecc",
                        elapsedTime = 0,
                        totalTime = 3000,
                    ),
                    TimerSegment(
                        name = "Iso",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Con",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Rest",
                        elapsedTime = 0,
                        totalTime = 3000
                    )
                ),
                set = LBSet(
                    id = "",
                    variationId = "",
                    reps = 4,
                    weight = 120.0
                )
            ),
            onEvent = {},
        )
    }
}

@Preview
@Composable
fun TimerRunningScreen_Preview() {
    PreviewAppTheme(isDarkMode = true) {
        TimerScreen(
            state = TimerState.Running(
                elapsedTime = 1,
                timers = listOf(
                    TimerSegment(
                        name = "Setup",
                        elapsedTime = 0,
                        totalTime = 5000,
                    ),
                    TimerSegment(
                        name = "Ecc",
                        elapsedTime = 0,
                        totalTime = 3000,
                    ),
                    TimerSegment(
                        name = "Iso",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Con",
                        elapsedTime = 0,
                        totalTime = 1000,
                    ),
                    TimerSegment(
                        name = "Rest",
                        elapsedTime = 0,
                        totalTime = 3000
                    )
                ),
                paused = false,
                lastTickTime = Clock.System.now().minus(3, DateTimeUnit.SECOND),
                beep = false,
                set = null,
            ),
            onEvent = {},
        )
    }
}
