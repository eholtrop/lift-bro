package com.lift.bro.presentation.pose

import androidx.compose.runtime.Composable

sealed interface PoseModelState {
    data object NotDownloaded : PoseModelState
    data object Downloading : PoseModelState
    data class Downloaded(val modelPath: String) : PoseModelState
    data class Error(val message: String) : PoseModelState
}

expect class PoseModelRepository {
    suspend fun downloadModelIfNeeded(): Result<String>
    fun isModelDownloaded(): Boolean
    fun getModelPath(): String?
    fun clearModel()
}

expect class PoseModelRepositoryFactory {
    fun create(): PoseModelRepository
}

@Composable
expect fun rememberPoseModelRepository(): PoseModelRepositoryFactory
