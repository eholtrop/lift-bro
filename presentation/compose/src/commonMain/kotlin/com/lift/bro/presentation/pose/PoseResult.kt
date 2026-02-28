package com.lift.bro.presentation.pose

import kotlinx.serialization.Serializable

@Suppress("MagicNumber")
@Serializable
data class PosePoint(
    val type: LandmarkType,
    val x: Float,
    val y: Float,
    val z: Float,
    val confidence: Float,
)

@Serializable
enum class LandmarkType {
    NOSE,
    LEFT_EYE_INNER,
    LEFT_EYE,
    LEFT_EYE_OUTER,
    RIGHT_EYE_INNER,
    RIGHT_EYE,
    RIGHT_EYE_OUTER,
    LEFT_EAR,
    RIGHT_EAR,
    LEFT_MOUTH,
    RIGHT_MOUTH,
    LEFT_SHOULDER,
    RIGHT_SHOULDER,
    LEFT_ELBOW,
    RIGHT_ELBOW,
    LEFT_WRIST,
    RIGHT_WRIST,
    LEFT_PINKY,
    RIGHT_PINKY,
    LEFT_INDEX,
    RIGHT_INDEX,
    LEFT_THUMB,
    RIGHT_THUMB,
    LEFT_HIP,
    RIGHT_HIP,
    LEFT_KNEE,
    RIGHT_KNEE,
    LEFT_ANKLE,
    RIGHT_ANKLE,
    LEFT_HEEL,
    RIGHT_HEEL,
    LEFT_FOOT_INDEX,
    RIGHT_FOOT_INDEX,
}

@Serializable
data class PoseResult(
    val landmarks: List<PosePoint>,
    val timestamp: Long,
)

@Serializable
data class FormAnalysis(
    val isParallel: Boolean = false,
    val isAtBottom: Boolean = false,
    val formIssues: List<FormIssue> = emptyList(),
)

@Serializable
enum class FormIssue {
    KNEES_CAVING,
    BACK_ROUNDING,
    ELBOWS_FLARING,
    HIP_HINTS,
    ASYMMETRIC_HIPS,
    ASYMMETRIC_SHOULDERS,
    LOSING_BALANCE,
}

fun PoseResult.findLandmark(type: LandmarkType): PosePoint? {
    return landmarks.find { it.type == type }
}

@Suppress("CyclomaticComplexMethod", "ReturnCount", "ComplexCondition", "MagicNumber")
fun PoseResult.analyzeSquat(): FormAnalysis {
    val leftHip = findLandmark(LandmarkType.LEFT_HIP)
    val rightHip = findLandmark(LandmarkType.RIGHT_HIP)
    val leftKnee = findLandmark(LandmarkType.LEFT_KNEE)
    val rightKnee = findLandmark(LandmarkType.RIGHT_KNEE)
    val leftAnkle = findLandmark(LandmarkType.LEFT_ANKLE)
    val rightAnkle = findLandmark(LandmarkType.RIGHT_ANKLE)
    val leftShoulder = findLandmark(LandmarkType.LEFT_SHOULDER)
    val rightShoulder = findLandmark(LandmarkType.RIGHT_SHOULDER)

    if (leftHip == null || rightHip == null || leftKnee == null || rightKnee == null) {
        return FormAnalysis()
    }

    val minHipConfidence = minOf(leftHip.confidence, rightHip.confidence)
    val minKneeConfidence = minOf(leftKnee.confidence, rightKnee.confidence)
    if (minHipConfidence < 0.5f || minKneeConfidence < 0.5f) {
        return FormAnalysis()
    }

    val avgHipY = (leftHip.y + rightHip.y) / 2
    val avgKneeY = (leftKnee.y + rightKnee.y) / 2
    val isParallel = avgHipY >= avgKneeY

    val isAtBottom = if (leftAnkle != null && rightAnkle != null) {
        val avgAnkleY = (leftAnkle.y + rightAnkle.y) / 2
        val depth = (avgAnkleY - avgHipY) / (avgAnkleY - avgKneeY)
        depth > 0.8f
    } else {
        false
    }

    val issues = mutableListOf<FormIssue>()

    if (leftShoulder != null && rightShoulder != null) {
        val shoulderDiff = kotlin.math.abs(leftShoulder.y - rightShoulder.y)
        if (shoulderDiff > 0.05f) {
            issues.add(FormIssue.ASYMMETRIC_SHOULDERS)
        }
    }

    if (leftHip != null && rightHip != null) {
        val hipDiff = kotlin.math.abs(leftHip.y - rightHip.y)
        if (hipDiff > 0.05f) {
            issues.add(FormIssue.ASYMMETRIC_HIPS)
        }
    }

    return FormAnalysis(
        isParallel = isParallel,
        isAtBottom = isAtBottom,
        formIssues = issues,
    )
}

@Suppress("CyclomaticComplexMethod", "ReturnCount", "ComplexCondition", "MagicNumber")
fun PoseResult.analyzePushUp(): FormAnalysis {
    val leftShoulder = findLandmark(LandmarkType.LEFT_SHOULDER)
    val rightShoulder = findLandmark(LandmarkType.RIGHT_SHOULDER)
    val leftHip = findLandmark(LandmarkType.LEFT_HIP)
    val rightHip = findLandmark(LandmarkType.RIGHT_HIP)
    val leftAnkle = findLandmark(LandmarkType.LEFT_ANKLE)
    val rightAnkle = findLandmark(LandmarkType.RIGHT_ANKLE)
    val leftElbow = findLandmark(LandmarkType.LEFT_ELBOW)
    val rightElbow = findLandmark(LandmarkType.RIGHT_ELBOW)

    if (leftShoulder == null || rightShoulder == null ||
        leftHip == null || rightHip == null ||
        leftAnkle == null || rightAnkle == null
    ) {
        return FormAnalysis()
    }

    val minConfidence = listOf(
        leftShoulder.confidence,
        rightShoulder.confidence,
        leftHip.confidence,
        rightHip.confidence,
        leftAnkle.confidence,
        rightAnkle.confidence,
    ).min()
    if (minConfidence < 0.5f) {
        return FormAnalysis()
    }

    val bodyLineAngle = calculateAngle(
        leftShoulder.x,
        leftShoulder.y,
        leftHip.x,
        leftHip.y,
        leftAnkle.x,
        leftAnkle.y
    )
    val isAtBottom = if (leftElbow != null && rightElbow != null) {
        val avgElbowY = (leftElbow.y + rightElbow.y) / 2
        val avgShoulderY = (leftShoulder.y + rightShoulder.y) / 2
        val avgHipY = (leftHip.y + rightHip.y) / 2
        avgElbowY < avgHipY && avgElbowY > avgShoulderY
    } else {
        false
    }

    val issues = mutableListOf<FormIssue>()

    val shoulderHipDiff = kotlin.math.abs(leftShoulder.y - leftHip.y) - kotlin.math.abs(rightShoulder.y - rightHip.y)
    if (kotlin.math.abs(shoulderHipDiff) > 0.1f) {
        issues.add(FormIssue.ASYMMETRIC_HIPS)
    }

    val hipSag = if (leftHip.y > (leftShoulder.y + leftAnkle.y) / 2) true else false
    if (hipSag) {
        issues.add(FormIssue.HIP_HINTS)
    }

    return FormAnalysis(
        isParallel = bodyLineAngle > 160f,
        isAtBottom = isAtBottom,
        formIssues = issues,
    )
}

@Suppress("LongParameterList", "MagicNumber")
private fun calculateAngle(
    x1: Float,
    y1: Float,
    x2: Float,
    y2: Float,
    x3: Float,
    y3: Float
): Float {
    val angle1 = kotlin.math.atan2((y1 - y2).toDouble(), (x1 - x2).toDouble())
    val angle2 = kotlin.math.atan2((y3 - y2).toDouble(), (x3 - x2).toDouble())
    var angle = Math.toDegrees(angle2 - angle1).toFloat()
    if (angle < 0) angle += 360f
    if (angle > 180f) angle = 360f - angle
    return 180f - angle
}
