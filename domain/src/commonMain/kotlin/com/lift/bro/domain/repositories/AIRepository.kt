package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.ExerciseHistory
import com.lift.bro.domain.models.SetRecommendation
import com.lift.bro.domain.models.WorkoutHistory
import com.lift.bro.domain.models.WorkoutPreferences
import com.lift.bro.domain.models.WorkoutTemplate
import kotlinx.coroutines.flow.Flow

enum class ModelStatus {
    Available,
    Downloadable,
    Downloading,
    Unavailable,
}

sealed class DownloadProgress {
    data class Started(val totalBytes: Long) : DownloadProgress()
    data class Progress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress()
    data object Completed : DownloadProgress()
    data class Failed(val error: String) : DownloadProgress()
}

interface AIRepository {
    fun getModelStatus(): ModelStatus
    fun downloadModel(): Flow<DownloadProgress>
    suspend fun isModelReady(): Boolean
    suspend fun generate(prompt: String): Result<String>
}

interface WorkoutGenerator {
    suspend fun generateWorkout(
        history: WorkoutHistory,
        preferences: WorkoutPreferences
    ): Result<WorkoutTemplate>

    suspend fun suggestWeight(
        variationId: String,
        targetReps: Long,
        history: ExerciseHistory
    ): Result<SetRecommendation>
}
