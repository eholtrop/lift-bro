package com.lift.bro.ui.navigation

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

sealed interface Destination {

    @Serializable
    data object Unknown: Destination

    @Serializable
    data object Onboarding: Destination

    @Serializable
    object Dashboard : Destination

    @Serializable
    data class LiftDetails(val liftId: String) : Destination

    @Serializable
    data class EditLift(val liftId: String?) : Destination

    @Serializable
    data class VariationDetails(val variationId: String) : Destination

    @Serializable
    data class EditSet(
        val setId: String? = null,
        val liftId: String? = null,
        val variationId: String? = null
    ) : Destination

    @Serializable
    data class EditExcercise(
        val localDate: LocalDate,
        val variationId: String,
    ) : Destination

    @Serializable
    object Settings : Destination
}
