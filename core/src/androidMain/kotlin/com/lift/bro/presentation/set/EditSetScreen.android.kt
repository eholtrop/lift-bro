package com.lift.bro.presentation.set

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import java.util.concurrent.Executors

@Composable
actual fun PoseDetector() {
}


@ExperimentalGetImage
@Composable
fun PoseDetectionApp() {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            // Optionally, show a dialog or guide user to settings if permission is permanently denied
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            context.startActivity(intent)
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (hasCameraPermission) {
                CameraPreviewWithPoseDetection(lifecycleOwner, context)
            } else {
                Text("Camera permission is required for pose detection.")
                Spacer(Modifier.height(16.dp))
                Button(onClick = { cameraPermissionLauncher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Camera Permission")
                }
            }
        }
    }
}

@ExperimentalGetImage
@Composable
fun CameraPreviewWithPoseDetection(
    lifecycleOwner: LifecycleOwner,
    context: Context
) {
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val poseDetector: PoseDetector = remember {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptionsBase.STREAM_MODE)
            .build()
        PoseDetection.getClient(options)
    }

    val poseResult = remember { mutableStateOf<Pose?>(null) }
    val previewView = remember { PreviewView(context) }
    var imageWidth by remember { mutableStateOf(0) }
    var imageHeight by remember { mutableStateOf(0) }
    var isFrontCamera by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context = context)
        val cameraProvider = cameraProviderFuture.get() // Blocking call, consider async

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        val imageAnalyzer = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also {
                it.setAnalyzer(cameraExecutor) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val inputImage = InputImage.fromMediaImage(
                            mediaImage,
                            imageProxy.imageInfo.rotationDegrees
                        )

                        // Update image dimensions for overlay
                        if (imageWidth == 0 || imageHeight == 0) {
                            imageWidth = inputImage.width
                            imageHeight = inputImage.height
                        }

                        poseDetector.process(inputImage)
                            .addOnSuccessListener { pose ->
                                poseResult.value = pose
                            }
                            .addOnFailureListener { e ->
                                Log.e("PoseDetection", "Pose detection failed", e)
                                poseResult.value = null // Clear previous pose on failure
                            }
                            .addOnCompleteListener {
                                imageProxy.close()
                            }
                    } else {
                        imageProxy.close()
                    }
                }
            }

        val cameraSelector =
            if (isFrontCamera) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA

        try {
            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                imageAnalyzer
            )
        } catch (exc: Exception) {
            Log.e("CameraX", "Use case binding failed", exc)
        }

        onDispose {
            cameraExecutor.shutdown()
            cameraProvider.unbindAll()
            poseDetector.close() // Close the detector when not needed
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                previewView.apply {
                    layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Overlay for drawing pose landmarks
        val currentPose = poseResult.value
        if (currentPose != null && imageWidth > 0 && imageHeight > 0) {
            PoseGraphicOverlay(
                pose = currentPose,
                imageWidth = imageWidth,
                imageHeight = imageHeight,
                isFrontCamera = isFrontCamera
            )
        }

        // Camera switch button
        Button(
            onClick = { isFrontCamera = !isFrontCamera },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) {
            Text(if (isFrontCamera) "Switch to Back Camera" else "Switch to Front Camera")
        }
    }
}

@Composable
fun PoseGraphicOverlay(
    pose: Pose,
    imageWidth: Int,
    imageHeight: Int,
    isFrontCamera: Boolean
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val scaleX = size.width / imageWidth
        val scaleY = size.height / imageHeight

        val paint = Paint().apply {
            color = Color.GREEN
            strokeWidth = 8f
            style = Paint.Style.FILL_AND_STROKE
        }
        val linePaint = Paint().apply {
            color = Color.YELLOW
            strokeWidth = 6f
            style = Paint.Style.STROKE
        }

        // Draw landmarks
        for (landmark in pose.allPoseLandmarks) {
            val x = if (isFrontCamera) size.width - (landmark.position.x * scaleX) else (landmark.position.x * scaleX)
            val y = landmark.position.y * scaleY
            drawContext.canvas.nativeCanvas.drawCircle(x, y, 10f, paint)
        }

        // Draw connections (bones)
        val connections = listOf(
            // Torso
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_HIP),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_HIP),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP),

            // Left Arm
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),

            // Right Arm
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),

            // Left Leg
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),

            // Right Leg
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),

            // Face (simplified)
            Pair(PoseLandmark.NOSE, PoseLandmark.LEFT_EYE_INNER),
            Pair(PoseLandmark.LEFT_EYE_INNER, PoseLandmark.LEFT_EYE),
            Pair(PoseLandmark.LEFT_EYE, PoseLandmark.LEFT_EYE_OUTER),
            Pair(PoseLandmark.LEFT_EYE_OUTER, PoseLandmark.RIGHT_EYE_OUTER),
            Pair(PoseLandmark.RIGHT_EYE_OUTER, PoseLandmark.RIGHT_EYE),
            Pair(PoseLandmark.RIGHT_EYE, PoseLandmark.RIGHT_EYE_INNER),
            Pair(PoseLandmark.RIGHT_EYE_INNER, PoseLandmark.NOSE),

            Pair(PoseLandmark.LEFT_EAR, PoseLandmark.LEFT_EYE_OUTER),
            Pair(PoseLandmark.RIGHT_EAR, PoseLandmark.RIGHT_EYE_OUTER),

            // Hands (simplified)
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_THUMB),
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_PINKY),
            Pair(PoseLandmark.LEFT_WRIST, PoseLandmark.LEFT_INDEX),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_THUMB),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_PINKY),
            Pair(PoseLandmark.RIGHT_WRIST, PoseLandmark.RIGHT_INDEX),

            // Feet (simplified)
            Pair(PoseLandmark.LEFT_ANKLE, PoseLandmark.LEFT_HEEL),
            Pair(PoseLandmark.LEFT_HEEL, PoseLandmark.LEFT_FOOT_INDEX),
            Pair(PoseLandmark.RIGHT_ANKLE, PoseLandmark.RIGHT_HEEL),
            Pair(PoseLandmark.RIGHT_HEEL, PoseLandmark.RIGHT_FOOT_INDEX)
        )

        for (connection in connections) {
            val startLandmark = pose.getPoseLandmark(connection.first)
            val endLandmark = pose.getPoseLandmark(connection.second)

            if (startLandmark != null && endLandmark != null) {
                val startX = if (isFrontCamera) size.width - (startLandmark.position.x * scaleX) else (startLandmark.position.x * scaleX)
                val startY = startLandmark.position.y * scaleY
                val endX = if (isFrontCamera) size.width - (endLandmark.position.x * scaleX) else (endLandmark.position.x * scaleX)
                val endY = endLandmark.position.y * scaleY

                drawContext.canvas.nativeCanvas.drawLine(startX, startY, endX, endY, linePaint)
            }
        }
    }
}