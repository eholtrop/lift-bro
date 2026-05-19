package com.lift.bro.domain.ai

import com.lift.bro.domain.repositories.AIRepository

class DownloadAIModelUseCase(
    private val aiRepository: AIRepository
) {
    operator fun invoke() = aiRepository.downloadModel()
}
