@file:Suppress(
    "TooGenericExceptionCaught",
    "PrintStackTrace",
    "MagicNumber",
    "CyclomaticComplexMethod",
    "FunctionOnlyReturningConstant",
    "UnusedParameter",
    "SwallowedException",
    "UseCheckOrError",
    "NoTrailingSpaces"
)

package com.lift.bro.presentation.pose

import android.content.Context
import android.os.SystemClock
import androidx.camera.core.ImageProxy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

class PoseAnalyzer(modelPath: String, private val ctx: Context) {
    private var poseLandmarker: PoseLandmarker? = null
    private val _poseResult = MutableStateFlow<PoseResult?>(null)
    val poseResult: StateFlow<PoseResult?> = _poseResult.asStateFlow()

    init {
        try {
            val modelName = "pose_landmarker_lite.task"

            val baseOptions = try {
                com.google.mediapipe.tasks.core.BaseOptions.builder()
                    .setModelAssetPath(modelName)
                    .build()
            } catch (e: Exception) {
                val modelFile = File(modelPath)
                if (!modelFile.exists()) {
                    throw IllegalStateException("Model file not found at: $modelPath")
                }
                throw IllegalStateException("Please bundle pose_landmarker_lite.task in assets")
            }

            val options = PoseLandmarker.PoseLandmarkerOptions.builder()
                .setBaseOptions(baseOptions)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { result, _ ->
                    _poseResult.value = result?.toPoseResult()
                }
                .build()

            poseLandmarker = PoseLandmarker.createFromOptions(ctx, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun analyzeImageProxy(imageProxy: ImageProxy): PoseResult? {
        val mediaImage = imageProxy.image ?: run {
            imageProxy.close()
            return null
        }

        val bitmap = mediaImageToBitmap(mediaImage)
        val mpImage = BitmapImageBuilder(bitmap).build()
        val timestamp = SystemClock.uptimeMillis()

        try {
            poseLandmarker?.detectAsync(mpImage, timestamp)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        imageProxy.close()
        return _poseResult.value
    }

    @Suppress("MagicNumber")
    private fun mediaImageToBitmap(mediaImage: android.media.Image): android.graphics.Bitmap {
        val yBuffer = mediaImage.planes[0].buffer
        val uBuffer = mediaImage.planes[1].buffer
        val vBuffer = mediaImage.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = android.graphics.YuvImage(
            nv21,
            android.graphics.ImageFormat.NV21,
            mediaImage.width,
            mediaImage.height,
            null
        )

        val out = java.io.ByteArrayOutputStream()
        yuvImage.compressToJpeg(
            android.graphics.Rect(0, 0, mediaImage.width, mediaImage.height),
            90,
            out
        )
        val imageBytes = out.toByteArray()

        return android.graphics.BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun com.google.mediapipe.tasks.vision.poselandmarker.PoseLandmarkerResult.toPoseResult(): PoseResult? {
        val landmarks = this.landmarks()
        if (landmarks.isEmpty()) return null

        val poseLandmarks = landmarks[0]
        val posePoints = poseLandmarks.mapIndexed { index, landmark ->
            PosePoint(
                type = indexToLandmarkType(index),
                x = landmark.x(),
                y = landmark.y(),
                z = landmark.z(),
                confidence = landmark.x()
            )
        }

        return PoseResult(
            landmarks = posePoints,
            timestamp = System.currentTimeMillis(),
        )
    }

    @Suppress("CyclomaticComplexMethod", "MagicNumber")
    private fun indexToLandmarkType(index: Int): LandmarkType {
        return when (index) {
            0 -> LandmarkType.NOSE
            1 -> LandmarkType.LEFT_EYE_INNER
            2 -> LandmarkType.LEFT_EYE
            3 -> LandmarkType.LEFT_EYE_OUTER
            4 -> LandmarkType.RIGHT_EYE_INNER
            5 -> LandmarkType.RIGHT_EYE
            6 -> LandmarkType.RIGHT_EYE_OUTER
            7 -> LandmarkType.LEFT_EAR
            8 -> LandmarkType.RIGHT_EAR
            9 -> LandmarkType.LEFT_MOUTH
            10 -> LandmarkType.RIGHT_MOUTH
            11 -> LandmarkType.LEFT_SHOULDER
            12 -> LandmarkType.RIGHT_SHOULDER
            13 -> LandmarkType.LEFT_ELBOW
            14 -> LandmarkType.RIGHT_ELBOW
            15 -> LandmarkType.LEFT_WRIST
            16 -> LandmarkType.RIGHT_WRIST
            17 -> LandmarkType.LEFT_PINKY
            18 -> LandmarkType.RIGHT_PINKY
            19 -> LandmarkType.LEFT_INDEX
            20 -> LandmarkType.RIGHT_INDEX
            21 -> LandmarkType.LEFT_THUMB
            22 -> LandmarkType.RIGHT_THUMB
            23 -> LandmarkType.LEFT_HIP
            24 -> LandmarkType.RIGHT_HIP
            25 -> LandmarkType.LEFT_KNEE
            26 -> LandmarkType.RIGHT_KNEE
            27 -> LandmarkType.LEFT_ANKLE
            28 -> LandmarkType.RIGHT_ANKLE
            29 -> LandmarkType.LEFT_HEEL
            30 -> LandmarkType.RIGHT_HEEL
            31 -> LandmarkType.LEFT_FOOT_INDEX
            32 -> LandmarkType.RIGHT_FOOT_INDEX
            else -> LandmarkType.NOSE
        }
    }

    fun close() {
        poseLandmarker?.close()
        poseLandmarker = null
    }

    companion object {
        @Suppress("FunctionOnlyReturningConstant", "UnusedParameter")
        fun analyze(frame: ByteArray, width: Int, height: Int, rotation: Int): PoseResult? {
            return null
        }
    }
}

class PoseAnalyzerFactory {
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    fun create(modelPath: String): PoseAnalyzer {
        return PoseAnalyzer(modelPath, context!!)
    }
}

@Composable
fun rememberPoseAnalyzerFactory(): PoseAnalyzerFactory {
    val ctx = androidx.compose.ui.platform.LocalContext.current
    return remember {
        PoseAnalyzerFactory().also { it.setContext(ctx) }
    }
}
