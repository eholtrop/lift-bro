package com.lift.bro.presentation.camera

import android.content.Context
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.FileOutputOptions
import androidx.camera.video.FallbackStrategy
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

actual class CameraPermission

@Composable
fun rememberCameraPermission(): CameraPermission {
    return remember { CameraPermission() }
}

actual class CameraControllerFactory {
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    actual fun create(): CameraController {
        return AndroidCameraController(context!!)
    }
}

@Composable
actual fun rememberCameraControllerFactory(): CameraControllerFactory {
    val context = LocalContext.current
    return remember {
        CameraControllerFactory().also { it.setContext(context) }
    }
}

class AndroidCameraController(
    private val context: Context,
) : CameraController {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _recordingComplete = MutableStateFlow<String?>(null)
    override val recordingComplete: StateFlow<String?> = _recordingComplete.asStateFlow()

    private var cameraProvider: ProcessCameraProvider? = null
    private var previewView: PreviewView? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var isInitialized = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalGetImage::class)
    fun setupCamera(
        previewView: PreviewView,
        lifecycleOwner: LifecycleOwner,
    ) {
        if (isInitialized) return
        
        this.previewView = previewView
        this.lifecycleOwner = lifecycleOwner
        
        val mainExecutor = ContextCompat.getMainExecutor(context)
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            try {
                cameraProvider = cameraProviderFuture.get()
                
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val recorder = Recorder.Builder()
                    .setQualitySelector(
                        QualitySelector.from(
                            Quality.HD,
                            FallbackStrategy.higherQualityOrLowerThan(Quality.HD)
                        )
                    )
                    .build()

                videoCapture = VideoCapture.withOutput(recorder)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider?.unbindAll()
                cameraProvider?.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    videoCapture
                )
                
                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mainExecutor)
    }

    fun isReady(): Boolean = cameraProvider != null

    override fun startRecording(outputFile: File) {
        val videoCapture = this.videoCapture ?: return

        val outputOptions = FileOutputOptions.Builder(outputFile).build()

        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .start(ContextCompat.getMainExecutor(context)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> {
                        _isRecording.value = true
                    }
                    is VideoRecordEvent.Finalize -> {
                        _isRecording.value = false
                        if (event.hasError()) {
                            _recordingComplete.value = null
                        } else {
                            _recordingComplete.value = outputFile.absolutePath
                        }
                    }
                }
            }
    }

    override fun stopRecording() {
        recording?.stop()
        recording = null
    }

    override fun release() {
        recording?.stop()
        cameraProvider?.unbindAll()
        isInitialized = false
    }
}

@Composable
actual fun CameraPreview(
    controller: CameraController,
    modifier: Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraController = controller as? AndroidCameraController

    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    LaunchedEffect(cameraController, previewView, lifecycleOwner) {
        if (cameraController != null && previewView != null && lifecycleOwner != null) {
            cameraController.setupCamera(previewView!!, lifecycleOwner)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.release()
        }
    }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                scaleType = PreviewView.ScaleType.FILL_CENTER
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            }
        },
        modifier = modifier,
        update = { view ->
            previewView = view
        }
    )
}
