package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.vinceglb.filekit.PlatformFile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVCaptureDeviceInput
import platform.AVFoundation.AVCaptureDevicePositionFront
import platform.AVFoundation.AVCaptureDeviceTypeBuiltInWideAngleCamera
import platform.AVFoundation.AVCaptureFileOutput
import platform.AVFoundation.AVCaptureFileOutputRecordingDelegateProtocol
import platform.AVFoundation.AVCaptureMovieFileOutput
import platform.AVFoundation.AVCaptureSession
import platform.AVFoundation.AVCaptureSessionPresetHigh
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.AVFoundation.AVLayerVideoGravityResizeAspectFill
import platform.AVFoundation.AVMediaTypeAudio
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.defaultDeviceWithDeviceType
import platform.Foundation.NSError
import platform.Foundation.NSURL
import platform.QuartzCore.CALayer
import platform.UIKit.UIView
import platform.darwin.NSObject
import kotlin.concurrent.Volatile

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

class IosCameraController: CameraController {
    private val captureSession = AVCaptureSession()
    private val movieOutput = AVCaptureMovieFileOutput()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    @Volatile
    private var isRunning = false
    private var recordingDelegate: CameraRecordingDelegate? = null

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingComplete = MutableStateFlow<String?>(null)
    override val recordingComplete: StateFlow<String?> = _recordingComplete.asStateFlow()

    fun setupPreview(view: UIView) {
        if (isRunning) return

        val layer = AVCaptureVideoPreviewLayer(session = captureSession)
        layer.videoGravity = AVLayerVideoGravityResizeAspectFill
        layer.frame = view.bounds
        view.layer.addSublayer(layer)
        previewLayer = layer

        configureSession()
//        Thread {
            captureSession.startRunning()
            isRunning = true
//        }.start()
    }

    private fun configureSession() {
        captureSession.beginConfiguration()
        captureSession.sessionPreset = AVCaptureSessionPresetHigh

        val cameraDevice = AVCaptureDevice.defaultDeviceWithDeviceType(
            AVCaptureDeviceTypeBuiltInWideAngleCamera,
            AVMediaTypeVideo,
            AVCaptureDevicePositionFront,
        )
        cameraDevice?.let { device ->
            val input = AVCaptureDeviceInput(device, null)
            if (captureSession.canAddInput(input)) {
                captureSession.addInput(input)
            }
        }

        val audioDevice = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeAudio)
        audioDevice?.let { device ->
            val input = AVCaptureDeviceInput(device, null)
            if (captureSession.canAddInput(input)) {
                captureSession.addInput(input)
            }
        }

        if (captureSession.canAddOutput(movieOutput)) {
            captureSession.addOutput(movieOutput)
        }

        captureSession.commitConfiguration()
    }

    override fun startRecording(outputFile: PlatformFile) {
        recordingDelegate = CameraRecordingDelegate(
            onStarted = { _isRecording.value = true },
            onFinished = { path ->
                _isRecording.value = false
                _recordingComplete.value = path
            },
        )
        movieOutput.startRecordingToOutputFileURL(outputFile.nsUrl, recordingDelegate!!)
    }

    override fun stopRecording() {
        movieOutput.stopRecording()
    }

    override fun release() {
        captureSession.stopRunning()
        isRunning = false
        _isRecording.value = false
        _recordingComplete.value = null
    }
}

private class CameraRecordingDelegate(
    private val onStarted: () -> Unit,
    private val onFinished: (String?) -> Unit,
): NSObject(), AVCaptureFileOutputRecordingDelegateProtocol {

    override fun captureOutput(
        captureOutput: AVCaptureFileOutput,
        didFinishRecordingToOutputFileAtURL: NSURL,
        fromConnections: List<*>,
        error: NSError?,
    ) {
        onFinished(if (error == null) didFinishRecordingToOutputFileAtURL.path else null)
    }
}

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    val iosController = controller as IosCameraController

    DisposableEffect(Unit) {
        onDispose {
            controller.release()
        }
    }

    UIKitView(
        factory = {
            UIView().apply {
                iosController.setupPreview(this)
            }
        },
        update = { view ->
            view.layer.sublayers?.forEach { (it as? CALayer)?.frame = view.bounds }
        },
        modifier = modifier,
    )
}
