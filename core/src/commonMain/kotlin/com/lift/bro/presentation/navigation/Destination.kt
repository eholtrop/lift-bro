package com.lift.bro.presentation.navigation

import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.Serializable

sealed interface Destination {
    @Serializable
    object Dashboard : Destination

    @Serializable
    data class LiftDetails(val liftId: String) : Destination

    @Serializable
    data class EditLift(val liftId: String?) : Destination

    @Serializable
    data class VariationDetails(val variationId: String) : Destination

    @Serializable
    data class EditVariation(val variationId: String) : Destination

    @Serializable
    data class EditSet(
        val setId: String? = null,
        val liftId: String? = null,
        val variationId: String? = null
    ) : Destination

    @Serializable
    object Settings : Destination
}
