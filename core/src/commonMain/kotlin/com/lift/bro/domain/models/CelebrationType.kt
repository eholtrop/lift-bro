package com.lift.bro.domain.models

sealed class CelebrationType {
    data object None: CelebrationType()
    data class NewOneRepMax(val old: LBSet, val new: LBSet): CelebrationType()
    data class NewEMax(val old: LBSet, val new: LBSet): CelebrationType()
}