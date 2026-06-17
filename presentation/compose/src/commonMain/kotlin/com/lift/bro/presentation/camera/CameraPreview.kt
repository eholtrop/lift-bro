package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

expect class CameraPermission

interface CameraController {
    fun startRecording(outputFile: PlatformFile)
    fun stopRecording()
    fun release()
    val isRecording: kotlinx.coroutines.flow.StateFlow<Boolean>
    val recordingComplete: kotlinx.coroutines.flow.StateFlow<String?>
}

expect class CameraControllerFactory {
    fun create(): CameraController
}

@Composable
expect fun rememberCameraControllerFactory(): CameraControllerFactory

@Composable expect fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)
