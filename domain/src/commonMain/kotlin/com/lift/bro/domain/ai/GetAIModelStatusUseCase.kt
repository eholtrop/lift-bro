package com.lift.bro.domain.ai

import com.lift.bro.domain.repositories.AIRepository

class GetAIModelStatusUseCase(
    private val aiRepository: AIRepository,
) {
    operator fun invoke() = aiRepository.getModelStatus()
}
