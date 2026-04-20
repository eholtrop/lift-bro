package com.lift.bro.data.core.ai

import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.lift.bro.domain.repositories.AIRepository
import com.lift.bro.domain.repositories.DownloadProgress
import com.lift.bro.domain.repositories.ModelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class MLKitAIRepository : AIRepository {

    private val generativeModel by lazy { Generation.getClient() }

    override fun getModelStatus(): ModelStatus = runBlocking {
        when (generativeModel.checkStatus()) {
            FeatureStatus.AVAILABLE -> ModelStatus.Available
            FeatureStatus.DOWNLOADABLE -> ModelStatus.Downloadable
            FeatureStatus.DOWNLOADING -> ModelStatus.Downloading
            else -> ModelStatus.Unavailable
        }
    }

    override fun downloadModel(): Flow<DownloadProgress> =
        generativeModel.download().map { status ->
            when (status) {
                is DownloadStatus.DownloadStarted -> DownloadProgress.Started(
                    totalBytes = 0L
                )
                is DownloadStatus.DownloadProgress -> DownloadProgress.Progress(
                    bytesDownloaded = status.totalBytesDownloaded,
                    totalBytes = 0L
                )
                DownloadStatus.DownloadCompleted -> DownloadProgress.Completed
                is DownloadStatus.DownloadFailed -> DownloadProgress.Failed(
                    error = status.e.message ?: "Download failed"
                )
            }
        }

    override suspend fun isModelReady(): Boolean =
        generativeModel.checkStatus() == FeatureStatus.AVAILABLE

    override suspend fun generate(prompt: String): Result<String> = runCatching {
        val response = generativeModel.generateContent(prompt)
        response.candidates.firstOrNull()?.text
            ?: error("Empty response from Gemini Nano")
    }
}
