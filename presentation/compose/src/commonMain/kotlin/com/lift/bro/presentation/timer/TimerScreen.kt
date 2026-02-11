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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
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
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.currentWindowDpSize
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
import chaintech.videoplayer.host.MediaPlayerHost
import chaintech.videoplayer.ui.video.VideoPlayerComposable
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.LocalShowMERCalcs
import com.lift.bro.presentation.LocalTwmSettings
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.LiftingScaffold
import com.lift.bro.ui.card.lift.weightFormat
import com.lift.bro.ui.dialog.InfoSpeechBubble
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.PreviewAppTheme
import io.github.l2hyunwoo.compose.camera.core.CameraConfiguration
import io.github.l2hyunwoo.compose.camera.core.CameraController
import io.github.l2hyunwoo.compose.camera.core.CameraLens
import io.github.l2hyunwoo.compose.camera.core.Directory
import io.github.l2hyunwoo.compose.camera.core.PermissionResult
import io.github.l2hyunwoo.compose.camera.core.rememberCameraPermissionManager
import io.github.l2hyunwoo.compose.camera.ui.CameraPreview
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.absolutePath
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.list
import io.github.vinceglb.filekit.name
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.minus
import org.jetbrains.compose.ui.tooling.preview.Preview
import tv.dpal.compose.isOpen
import tv.dpal.logging.Log
import tv.dpal.logging.d

@Composable
fun getFileUrlFromContentUri(uri: String): String? {
    val fileName = uri.split("/").last()

    return (FileKit.cacheDir / "video_cache" / fileName).let {
        (FileKit.cacheDir / "video_cache").list().forEach {
            Log.d(message = it.name)
        }
        if (it.exists()) {
            Log.d(message = "exists")
            return it.absolutePath()
        } else {
            Log.d(message = "nope")
            null
        }
    }
}

@Composable
fun TimerScreen(
    reps: Int,
    tempo: Tempo,
) {
    var cameraController by remember {
        mutableStateOf<CameraController?>(null)
    }
    val interactor: TimerInteractor = rememberTimerInteractor(reps, tempo) { disp, state, event ->
        cameraController?.let { controller ->
            when (event) {
                TimerEvent.Plan.Start -> {
                    Log.d(message = "START RECORDING")
                    disp(TimerEvent.Running.RecordingStarted(controller.startRecording()))
                }

                else -> {}
            }
            if (state is TimerState.Running) {
                when (event) {
                    TimerEvent.Running.Pause -> {
                        state.recording?.pause()
                    }

                    TimerEvent.Running.Resume -> {
                        state.recording?.resume()
                    }

                    else -> {}
                }
            }
        }
    }

    TimerScreen(
        state = interactor.state.collectAsState().value,
        onEvent = interactor::invoke,
        onCameraControllerReady = {
            cameraController = it
        }
    )
}

@Composable
fun TimerScreen(
    setId: String,
) {
    var cameraController by remember {
        mutableStateOf<CameraController?>(null)
    }
    val interactor: TimerInteractor = rememberTimerInteractor(setId) { disp, state, event ->
        cameraController?.let { controller ->
            when (event) {
                TimerEvent.Plan.Start -> {
                    Log.d(message = "START RECORDING")
                    disp(TimerEvent.Running.RecordingStarted(controller.startRecording()))
                }

                else -> {}
            }
            if (state is TimerState.Running) {
                when (event) {
                    TimerEvent.Running.Pause -> {
                        state.recording?.pause()
                    }

                    TimerEvent.Running.Resume -> {
                        state.recording?.resume()
                    }

                    else -> {}
                }
            }
        }
    }

    TimerScreen(
        state = interactor.state.collectAsState().value,
        onEvent = interactor::invoke,
        onCameraControllerReady = {
            cameraController = it
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(
    state: TimerState,
    onEvent: (TimerEvent) -> Unit,
    onCameraControllerReady: (CameraController) -> Unit,
) {
    var showCamera by remember { mutableStateOf(false) }
    var cameraController by remember { mutableStateOf<CameraController?>(null) }
    Box(
        contentAlignment = Alignment.Center,
    ) {
        val permissionManager = rememberCameraPermissionManager()
        var cameraPermissionRequest by remember { mutableStateOf<PermissionResult?>(null) }
        LaunchedEffect(showCamera) {
            if (showCamera) {
                cameraPermissionRequest = permissionManager.requestCameraPermissions()
                permissionManager
            }
        }

        if (showCamera && state !is TimerState.Ended) {
            if (cameraPermissionRequest?.cameraGranted == true) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    configuration = CameraConfiguration(
                        lens = CameraLens.FRONT,
                        directory = Directory.CACHE,
                    ),
                    onCameraControllerReady = onCameraControllerReady
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
                            is TimerState.Ended -> {
                                TimerTrack(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colorScheme.background)
                                        .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                    scrollable = true,
                                    segments = state.timers
                                )
                            }

                            is TimerState.Plan -> TimerTrack(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                segments = state.runningTimer.timers,
                                scrollable = true,
                            )

                            is TimerState.Running -> TimerTrack(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colorScheme.background)
                                    .padding(bottom = MaterialTheme.spacing.threeQuarters),
                                segments = state.timers,
                                scrollable = state.paused
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
                onEvent = onEvent,
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

                    state.recording?.let {
                        item {
                            Log.d(message = "DOING STUFF")
                            Log.d(message = state.recording)
                            val file = getFileUrlFromContentUri(state.recording)
                            Log.d(message = file ?: "")
                            VideoPlayerComposable(
                                modifier = Modifier.height(256.dp)
                                    .aspectRatio(currentWindowDpSize().width.value / currentWindowDpSize().height.value),
                                playerHost = MediaPlayerHost(
                                    mediaUrl = file ?: "",
                                    autoPlay = true
                                )
                            )
                        }
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
            onCameraControllerReady = {}
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
            onCameraControllerReady = {}
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
            onCameraControllerReady = {}
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
            onCameraControllerReady = {}
        )
    }
}
