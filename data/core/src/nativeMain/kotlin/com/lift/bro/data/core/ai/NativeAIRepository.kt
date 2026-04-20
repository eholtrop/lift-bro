package com.lift.bro.data.core.ai

import com.lift.bro.domain.repositories.AIRepository
import com.lift.bro.domain.repositories.DownloadProgress
import com.lift.bro.domain.repositories.ModelStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class NativeAIRepository : AIRepository {

    override fun getModelStatus(): ModelStatus = ModelStatus.Unavailable

    override fun downloadModel(): Flow<DownloadProgress> = flow {
        emit(DownloadProgress.Failed("Local AI not supported on this platform - using rule-based fallback"))
    }

    override suspend fun isModelReady(): Boolean = false

    override suspend fun generate(prompt: String): Result<String> {
        return Result.failure(NotImplementedError("Local AI not supported on this platform"))
    }
}
