package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import platform.Foundation.NSFileManager

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
    override val recordingComplete: kotlinx.coroutines.flow.StateFlow<String?> = _recordingComplete.asAsStateFlow()

    override fun startRecording(outputFile: java.io.File) {
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

private fun <T> kotlinx.coroutines.flow.MutableStateFlow<T>.asAsStateFlow(): kotlinx.coroutines.flow.StateFlow<T> = this
