package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.asStateFlow

actual class CameraPermission

actual class CameraControllerFactory {
    actual fun create(): CameraController {
        return IosCameraController()
    }
}

@Composable
actual fun rememberCameraControllerFactory(): CameraControllerFactory {
    return remember { CameraControllerFactory() }
}

class IosCameraController : CameraController {
    private val _isRecording = kotlinx.coroutines.flow.MutableStateFlow(false)
    override val isRecording: kotlinx.coroutines.flow.StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingComplete = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)
    override val recordingComplete: kotlinx.coroutines.flow.StateFlow<String?> = _recordingComplete.asStateFlow()

    override fun startRecording(outputFile: PlatformFile) {
        _isRecording.value = true
    }

    override fun stopRecording() {
        _isRecording.value = false
    }

    override fun release() {
    }
}

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    DisposableEffect(Unit) {
        onDispose {
            controller.release()
        }
    }
}
