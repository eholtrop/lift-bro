package com.lift.bro.ui.navigation

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
sealed interface Destination {

    @Serializable
    data object Unknown: Destination

    @Serializable
    data object Onboarding: Destination

    @Serializable
    data object Home: Destination

    @Serializable
    data class LiftDetails(val liftId: String): Destination

    @Serializable
    data class EditLift(val liftId: String?): Destination

    @Serializable
    data class VariationDetails(val variationId: String): Destination

    @Serializable
    data class EditSet(
        val setId: String,
    ): Destination

    @Serializable
    data class CreateSet(
        val liftId: String? = null,
        val variationId: String? = null,
        val date: Instant? = null,
    ): Destination

    @Serializable
    data class EditWorkout(
        val localDate: LocalDate,
    ): Destination

    @Serializable
    data class CreateWorkout(
        val localDate: LocalDate,
    ): Destination

    @Serializable
    data object Settings: Destination

    @Serializable
    data class Wrapped(val year: Int): Destination

    @Serializable
    data object Goals: Destination
}
