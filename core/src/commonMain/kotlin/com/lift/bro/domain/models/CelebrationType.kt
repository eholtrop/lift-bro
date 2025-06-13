package com.lift.bro.domain.models

sealed class CelebrationType {
    data object None: CelebrationType()
    data object FirstLift: CelebrationType()
    data object FirstVariation: CelebrationType()
    data class NewVariationMax(val variationName: String): CelebrationType()
    data class NewLiftMax(val liftName: String): CelebrationType()
}