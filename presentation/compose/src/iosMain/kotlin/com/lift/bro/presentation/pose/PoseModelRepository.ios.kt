package com.lift.bro.presentation.pose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Suppress("EmptyFunctionBlock")
actual class PoseModelRepository {
    actual fun isModelDownloaded(): Boolean = false

    actual fun getModelPath(): String? = null

    actual suspend fun downloadModelIfNeeded(): Result<String> {
        return Result.failure(NotImplementedError("Pose detection not supported on iOS"))
    }

    actual fun clearModel() { }
}

actual class PoseModelRepositoryFactory {
    actual fun create(): PoseModelRepository {
        return PoseModelRepository()
    }
}

@Composable
actual fun rememberPoseModelRepository(): PoseModelRepositoryFactory {
    return remember { PoseModelRepositoryFactory() }
}
