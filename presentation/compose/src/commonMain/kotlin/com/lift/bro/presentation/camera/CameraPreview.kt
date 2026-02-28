package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.lift.bro.presentation.pose.PoseResult
import java.io.File

expect class CameraPermission

interface CameraController {
    fun startRecording(outputFile: File)
    fun stopRecording()
    fun release()
    val isRecording: kotlinx.coroutines.flow.StateFlow<Boolean>
    val recordingComplete: kotlinx.coroutines.flow.StateFlow<String?>
    val poseResult: kotlinx.coroutines.flow.StateFlow<PoseResult?>
}

expect class CameraControllerFactory {
    fun create(modelPath: String? = null): CameraController
}

@Composable
expect fun rememberCameraControllerFactory(): CameraControllerFactory

@Composable expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)
