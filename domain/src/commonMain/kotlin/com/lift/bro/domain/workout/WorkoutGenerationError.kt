package com.lift.bro.domain.workout

sealed class WorkoutGenerationError : Exception() {
    data object ModelNotReady : WorkoutGenerationError()
    data object GenerationFailed : WorkoutGenerationError()
    data object ParseFailed : WorkoutGenerationError()
}

