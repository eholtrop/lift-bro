package com.lift.bro.presentation.timer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Speaker
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.AnimatedText
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.Space
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
import lift_bro.core.generated.resources.timer_screen_set_count_cta
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.tooling.preview.Preview
import tv.dpal.compose.isOpen
import tv.dpal.ext.ktx.datetime.toString

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
                AnimatedVisibility(
                    visible = !LocalSoftwareKeyboardController.current.isOpen(),
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it },
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        when (state) {
                            is TimerState.Ended -> {}
                            is TimerState.Plan -> TimerTrack(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                state = state.runningTimer,
                                scrollable = true,
                            )

                            is TimerState.Running -> TimerTrack(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                state = state,
                            )
                        }
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
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val textStyle = MaterialTheme.typography.displayMedium
                    val textModifier = textModifier()
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

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun PlanTimerContent(
    modifier: Modifier = Modifier,
    state: TimerState.Plan,
    onEvent: (TimerEvent) -> Unit,
) {
    var expanded by remember { mutableStateOf(state.tempo.any { t -> t != state.tempo.firstOrNull() }) }
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            all = MaterialTheme.spacing.one,
        ),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        item {
            TextField(
                modifier = textModifier().width(96.dp),
                value = state.startupTime.toString(),
                onValueChange = {
                    it.toLongOrNull()?.let {
                        onEvent(TimerEvent.Plan.StartupTimeChanged(it))
                    }
                },
                label = { Text("Ready") },
                textStyle = MaterialTheme.typography.displayMedium.copy(
                    textAlign = TextAlign.Center,
                ),
                colors = TextFieldDefaults.transparentColors(),
            )
        }

        item {
            Icon(
                modifier = Modifier.fillMaxWidth(),
                imageVector = Icons.Default.ArrowDownward,
                contentDescription = ""
            )
        }

        item {
            val tempo = state.tempo.first()
            Row(
                modifier = Modifier.animateItem(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TimerSetField(
                    state = tempo,
                    rest = state.perSetRest,
                    rep = if (!expanded) null else 0,
                    onEvent = onEvent,
                )
                if (!expanded) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        IconButton(
                            onClick = {
                                onEvent(TimerEvent.Plan.AddTimer)
                            }
                        ) {
                            Text("+")
                        }
                        Button(
                            onClick = {
                                expanded = true
                            },
                            colors = ButtonDefaults.textButtonColors(),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(
                                text = stringResource(Res.string.timer_screen_set_count_cta, state.tempo.size),
                                style = MaterialTheme.typography.displaySmall,
                            )
                        }
                        IconButton(
                            onClick = {
                                onEvent(TimerEvent.Plan.RemoveTimer())
                            },
                            enabled = state.tempo.size > 1
                        ) {
                            Text("-")
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            onEvent(TimerEvent.Plan.RemoveTimer(0))
                        },
                        enabled = state.tempo.size > 1
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Tempo",
                        )
                    }
                }
            }
        }

        if (expanded && state.tempo.isNotEmpty()) {
            itemsIndexed(
                state.tempo.drop(1),
            ) { i, tempo ->
                // increase index to match state since we dropped 1
                val index = i + 1
                Row(
                    modifier = Modifier.wrapContentHeight(),
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.quarter)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            TimerSetField(
                                rest = state.perSetRest,
                                rep = index,
                                state = tempo,
                                onEvent = onEvent
                            )
                            IconButton(
                                onClick = {
                                    onEvent(TimerEvent.Plan.RemoveTimer(index))
                                },
                                enabled = state.tempo.size > 1
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Tempo",
                                )
                            }
                        }
                    }
                }
            }
        }

        item {
            if (expanded) {
                Row {
                    IconButton(
                        onClick = {
                            onEvent(TimerEvent.Plan.AddTimer)
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Tempo"
                        )
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
                        name = "Rest",
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
