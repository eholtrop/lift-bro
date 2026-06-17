package com.lift.bro.presentation.recording

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.camera.CameraPreview
import com.lift.bro.presentation.camera.rememberCameraPermissionHandler
import com.lift.bro.presentation.recording.RecordSetEvent.RecordedEvent.ReRecordVideo
import com.lift.bro.presentation.recording.RecordSetEvent.RecordedEvent.SaveToSet
import com.lift.bro.presentation.video.VideoPlayer
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import tv.dpal.compose.padding.vertical.padding

@Composable
fun RecordSetScreen(
    interactor: RecordingInteractor,
) {
    val state by interactor.state.collectAsState()

    RecordSetScreen(
        state = state,
        onEvent = { interactor(it) }
    )
}

@Composable
fun RecordSetScreen(
    state: RecordSetState,
    onEvent: (RecordSetEvent) -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Handle Camera / Video
            when (state) {
                is RecordSetState.Recorded -> {
                    state.videoUri?.let { uri ->
                        dependencies.videoStorage.getVideoFile(uri)?.let { file ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(all = MaterialTheme.spacing.two)
                            ) {
                                VideoPlayer(
                                    videoFile = file,
                                    modifier = Modifier.fillMaxSize()
                                        .clip(MaterialTheme.shapes.large)
                                )
                            }
                        }
                    }
                }

                else -> state.cameraController?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        CameraPreview(
                            controller = it,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            AnimatedVisibility(
                visible = state is RecordSetState.Recorded,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { -it }
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = MaterialTheme.spacing.two,
                    ).navigationBarsPadding(),
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onEvent(ReRecordVideo)
                        },
                        colors = ButtonDefaults.elevatedButtonColors(),
                    ) {
                        Text(
                            text = "Re-Record"
                        )
                    }
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            onEvent(SaveToSet)
                        }
                    ) {
                        Text(
                            text = "Save"
                        )
                    }
                }
            }
        }

        // Footer
        if (state !is RecordSetState.Recorded) {
            Column(
                modifier = Modifier.fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row {
                    if (state is RecordSetState.Planning) {
                        IconButton(
                            onClick = {
                                onEvent(RecordSetEvent.PlanningEvent.ToggleMetronome)
                            },
                        ) {
                            Icon(
                                imageVector = if (state.metronomeEnabled) Icons.Default.Timer else Icons.Default.TimerOff,
                                contentDescription = "Toggle Metronome",
                            )
                        }
                        Space()
                        IconButton(
                            onClick = {
                                onEvent(RecordSetEvent.PlanningEvent.ToggleCamera)
                            }
                        ) {
                            val cameraPermission = rememberCameraPermissionHandler()
                            LaunchedEffect(state.cameraEnabled, cameraPermission.isGranted) {
                                if (state.cameraEnabled) {
                                    if (cameraPermission.isGranted) {
                                        onEvent(RecordSetEvent.PlanningEvent.CameraPermissionGranted)
                                    } else {
                                        cameraPermission.requestPermission { }
                                    }
                                }
                            }
                            Icon(
                                imageVector = if (state.cameraEnabled) Icons.Default.Camera else Icons.Default.Cameraswitch,
                                contentDescription = "Toggle Camera",
                            )
                        }
                    }
                }

                val buttonCornerRadius = when (state) {
                    is RecordSetState.Planning -> 52.dp
                    is RecordSetState.Recording -> 8.dp
                }

                val buttonShape by animateDpAsState(buttonCornerRadius)

                Button(
                    onClick = {
                        when (state) {
                            is RecordSetState.Planning -> onEvent(RecordSetEvent.PlanningEvent.StartRecording)
                            is RecordSetState.Recorded -> {}
                            is RecordSetState.Recording -> onEvent(RecordSetEvent.RecordingEvent.Stop)
                        }
                    },
                    contentPadding = PaddingValues(MaterialTheme.spacing.half),
                    shape = RoundedCornerShape(buttonShape)
                ) {
                    Box(
                        modifier = Modifier.defaultMinSize(
                            minWidth = 52.dp,
                            minHeight = 52.dp,
                        )
                            .background(
                                color = MaterialTheme.colorScheme.onPrimary,
                                shape = RoundedCornerShape(buttonShape)
                            )
                    )
                }
            }
        }
    }
}
