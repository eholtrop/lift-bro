package com.lift.bro.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.presentation.set.TempoState
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
import com.lift.bro.ui.TempoSelector
import com.lift.bro.ui.TopBarIconButton
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import io.github.l2hyunwoo.compose.camera.core.PermissionResult
import io.github.l2hyunwoo.compose.camera.core.rememberCameraPermissionManager
import io.github.l2hyunwoo.compose.camera.ui.CameraPreview
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.minus
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.timer_screen_rest_reset_label
import lift_bro.core.generated.resources.timer_screen_set_count_cta
import lift_bro.core.generated.resources.timer_screen_startup_time_buffer_text
import lift_bro.core.generated.resources.timer_screen_title
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import tv.dpal.ext.ktx.datetime.toString

@Composable
fun TimerScreen(
    reps: Int,
    tempo: Tempo
) {

    val interactor: TimerInteractor = rememberTimerInteractor(reps, tempo)

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
    var showCamera by remember { mutableStateOf(false) }
    Box(
        contentAlignment = Alignment.Center,
    ) {
        val permissionManager = rememberCameraPermissionManager()
        var cameraPermissionRequest by remember { mutableStateOf<PermissionResult?>(null) }
        LaunchedEffect(showCamera) {
            if (showCamera) {
                cameraPermissionRequest = permissionManager.requestCameraPermissions()
            }
        }
        if (showCamera) {
            if (cameraPermissionRequest?.cameraGranted == true) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onCameraControllerReady = { controller ->

                    }
                )
            } else {
                Text("No Camera Permissions")
                Button(
                    onClick = {
                        permissionManager.openAppSettings()
                    }
                ) {
                    Text("Request Permissions")
                }
            }
        }
        LiftingScaffold(
            title = { },
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onBackground,
            fab = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    when (state) {
                        is TimerState.Ended -> {}
                        is TimerState.Plan -> TimerTrack(
                            modifier = Modifier.fillMaxWidth(),
                            state = state.runningTimer
                        )

                        is TimerState.Running -> TimerTrack(
                            modifier = Modifier.fillMaxWidth(),
                            state = state,
                        )
                    }
                    Space(MaterialTheme.spacing.threeQuarters)

                    Row {
                        IconButton(
                            onClick = {}
                        ) {
                            Icon(
                                imageVector = Icons.Default.Speaker,
                                contentDescription = "Play Sound"
                            )
                        }
                        Button(
                            modifier = Modifier.defaultMinSize(52.dp, 52.dp),
                            onClick = {
                                when (state) {
                                    is TimerState.Plan -> onEvent(TimerEvent.Plan.Start)
                                    is TimerState.Running -> if (state.paused) onEvent(TimerEvent.Running.Resume) else onEvent(TimerEvent.Running.Pause)
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
                                if (showCamera) {
                                    Text("/")
                                    Icon(
                                        imageVector = Icons.Default.RecordVoiceOver,
                                        contentDescription = "Record"
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = {
                                showCamera = !showCamera
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Camera,
                                contentDescription = "Record with Camera"
                            )
                        }
                    }
                }
            },
        ) { padding ->
            TimerOverlay(
                modifier = Modifier.padding(padding),
                state = state,
                onEvent = onEvent
            )
        }
    }
}

@Composable
fun TimerOverlay(
    modifier: Modifier = Modifier,
    state: TimerState,
    onEvent: (TimerEvent) -> Unit,
) {
    when (state) {
        is TimerState.Plan -> PlanTimerContent(
            modifier = modifier,
            state = state,
            onEvent = onEvent,
        )

        is TimerState.Running -> RunningTimerContent(
            modifier = modifier,
            state = state,
            onEvent = onEvent,
        )

        is TimerState.Ended -> {
            Text("Congrats!!")
        }
    }
}

@Composable
fun RunningTimerContent(
    modifier: Modifier = Modifier,
    state: TimerState.Running,
    onEvent: (TimerEvent) -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        state.currentTimer?.let { timer ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    timer.name,
                    style = MaterialTheme.typography.titleLarge,
                )
                Space(MaterialTheme.spacing.half)
                val remainingTime = (timer.totalTime - timer.elapsedTime) / 1000.0

                val remaining = Instant.fromEpochMilliseconds(timer.totalTime - timer.elapsedTime)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {


                    val textStyle = MaterialTheme.typography.displayMedium
                    val textModifier = Modifier.defaultMinSize(minWidth = 36.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = MaterialTheme.shapes.small,
                        )
                        .border(
                            width = Dp.Hairline,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = MaterialTheme.shapes.small,
                        )


                    AnimatedText(
                        modifier = textModifier,
                        style = textStyle,
                        text = remaining.toString("ss").take(1),
                        color = if (remainingTime <= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Space(MaterialTheme.spacing.eighth)
                    AnimatedText(
                        modifier = textModifier,
                        style = textStyle,
                        text = remaining.toString("ss").drop(1),
                        color = if (remainingTime <= 5) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                    Space(MaterialTheme.spacing.quarter)
                    Text(":")
                    Space(MaterialTheme.spacing.quarter)
                    Text(
                        modifier = textModifier,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        text = remaining.toString("SS").take(1)
                    )
                    Space(MaterialTheme.spacing.eighth)
                    Text(
                        modifier = textModifier,
                        style = textStyle,
                        textAlign = TextAlign.Center,
                        text = remaining.toString("SS").drop(1)
                    )
                }
            }
        }
    }
}

@Composable
fun PlanTimerContent(
    modifier: Modifier = Modifier,
    state: TimerState.Plan,
    onEvent: (TimerEvent) -> Unit,
) {
    LazyColumn(
        modifier = modifier,
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
                    label = { Text(stringResource(Res.string.timer_screen_startup_time_buffer_text)) },
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
                                Text(stringResource(Res.string.timer_screen_set_count_cta, 3))
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
                                label = { Text(stringResource(Res.string.timer_screen_rest_reset_label)) },
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
                                        label = { Text(stringResource(Res.string.timer_screen_rest_reset_label)) },
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
            state = TimerState.Plan(
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
            state = TimerState.Plan(
                tempo = listOf(Tempo(down = 10), Tempo(), Tempo())
            ),
            onEvent = {}
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
                        name = stringResource(Res.string.timer_screen_rest_reset_label),
                        elapsedTime = 0,
                        totalTime = 3000
                    )
                ),
                paused = false,
                lastTickTime = Clock.System.now().minus(3, DateTimeUnit.SECOND),
                beep = false,
            ),
            onEvent = {}
        )
    }
}
