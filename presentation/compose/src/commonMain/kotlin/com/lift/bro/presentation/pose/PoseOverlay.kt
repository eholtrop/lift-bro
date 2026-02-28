package com.lift.bro.presentation.pose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap

@Suppress("MagicNumber")
@Composable
fun PoseOverlay(
    poseResult: PoseResult?,
    modifier: Modifier = Modifier,
    skeletonColor: Color = Color.Green,
    landmarkRadius: Float = 8f,
) {
    if (poseResult == null) return

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width
        val scaleY = size.height

        val landmarks = poseResult.landmarks
            .filter { it.confidence > 0.5f }
            .associateBy { it.type }

        fun getPoint(type: LandmarkType): Offset? {
            val landmark = landmarks[type] ?: return null
            return Offset(
                x = landmark.x * scaleX,
                y = landmark.y * scaleY
            )
        }

        val connections = listOf(
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.RIGHT_SHOULDER),
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_ELBOW),
            Pair(LandmarkType.LEFT_ELBOW, LandmarkType.LEFT_WRIST),
            Pair(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_ELBOW),
            Pair(LandmarkType.RIGHT_ELBOW, LandmarkType.RIGHT_WRIST),
            Pair(LandmarkType.LEFT_SHOULDER, LandmarkType.LEFT_HIP),
            Pair(LandmarkType.RIGHT_SHOULDER, LandmarkType.RIGHT_HIP),
            Pair(LandmarkType.LEFT_HIP, LandmarkType.RIGHT_HIP),
            Pair(LandmarkType.LEFT_HIP, LandmarkType.LEFT_KNEE),
            Pair(LandmarkType.LEFT_KNEE, LandmarkType.LEFT_ANKLE),
            Pair(LandmarkType.RIGHT_HIP, LandmarkType.RIGHT_KNEE),
            Pair(LandmarkType.RIGHT_KNEE, LandmarkType.RIGHT_ANKLE),
        )

        connections.forEach { (start, end) ->
            val startPoint = getPoint(start)
            val endPoint = getPoint(end)

            if (startPoint != null && endPoint != null) {
                drawLine(
                    color = skeletonColor,
                    start = startPoint,
                    end = endPoint,
                    strokeWidth = 4f,
                    cap = StrokeCap.Round
                )
            }
        }

        landmarks.values.forEach { landmark ->
            val point = Offset(landmark.x * scaleX, landmark.y * scaleY)
            drawCircle(
                color = skeletonColor.copy(alpha = landmark.confidence),
                radius = landmarkRadius,
                center = point
            )
        }
    }
}

@Composable
fun FormFeedbackOverlay(
    poseResult: PoseResult?,
    analyzeSquat: Boolean = true,
    modifier: Modifier = Modifier,
) {
    if (poseResult == null) return

    val formAnalysis = if (analyzeSquat) {
        poseResult.analyzeSquat()
    } else {
        poseResult.analyzePushUp()
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        val scaleX = size.width
        val scaleY = size.height

        val leftHip = poseResult.findLandmark(LandmarkType.LEFT_HIP)
        val rightHip = poseResult.findLandmark(LandmarkType.RIGHT_HIP)
        val leftKnee = poseResult.findLandmark(LandmarkType.LEFT_KNEE)
        val rightKnee = poseResult.findLandmark(LandmarkType.RIGHT_KNEE)

        @Suppress("ComplexCondition", "MagicNumber", "UnusedPrivateProperty")
        if (leftHip != null && rightHip != null && leftKnee != null && rightKnee != null) {
            val avgHipY = (leftHip.y + rightHip.y) / 2
            val avgKneeY = (leftKnee.y + rightKnee.y) / 2

            val indicatorColor = if (formAnalysis.isParallel) Color.Green else Color.Yellow

            drawCircle(
                color = indicatorColor,
                radius = 20f,
                center = Offset(size.width - 50f, 80f)
            )
        }

        @Suppress("MagicNumber")
        if (formAnalysis.formIssues.isNotEmpty()) {
            val issueColor = Color.Red
            drawCircle(
                color = issueColor,
                radius = 20f,
                center = Offset(size.width - 50f, 130f)
            )
        }
    }
}
