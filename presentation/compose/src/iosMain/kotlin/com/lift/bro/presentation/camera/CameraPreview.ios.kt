package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.lift.bro.presentation.pose.PoseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual class CameraPermission

actual class CameraControllerFactory {
    actual fun create(modelPath: String?): CameraController {
        return IosCameraController()
    }
}

@Composable
actual fun rememberCameraControllerFactory(): CameraControllerFactory {
    return remember { CameraControllerFactory() }
}

class IosCameraController : CameraController {
    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingComplete = MutableStateFlow<String?>(null)
    override val recordingComplete: StateFlow<String?> = _recordingComplete.asStateFlow()

    private val _poseResult = MutableStateFlow<PoseResult?>(null)
    override val poseResult: StateFlow<PoseResult?> = _poseResult.asStateFlow()

    override fun startRecording(outputFile: java.io.File) {
        _isRecording.value = true
    }

    override fun stopRecording() {
        _isRecording.value = false
    }

@Suppress("EmptyFunctionBlock")
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
