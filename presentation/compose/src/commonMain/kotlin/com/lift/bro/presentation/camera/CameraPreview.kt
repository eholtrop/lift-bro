package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import java.io.File

expect class CameraPermission

interface CameraController {
    fun startRecording(outputFile: File)
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

expect @Composable
fun CameraPreview(
    controller: CameraController,
    modifier: Modifier = Modifier,
)
