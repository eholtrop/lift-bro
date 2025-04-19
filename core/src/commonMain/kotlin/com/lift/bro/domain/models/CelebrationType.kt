package com.lift.bro.domain.models
sealed class CelebrationType {
    data object None: CelebrationType()
    data object Variation: CelebrationType()
    data object Lift: CelebrationType()
}