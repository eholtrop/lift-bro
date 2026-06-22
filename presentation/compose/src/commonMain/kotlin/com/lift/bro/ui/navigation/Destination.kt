package com.lift.bro.ui.navigation

import com.benasher44.uuid.uuid4
import com.lift.bro.domain.serializers.InstantSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
sealed class Destination: tv.dpal.navi.Destination() {

    @Serializable
    data object Unknown: Destination()

    @Serializable
    data object Onboarding: Destination()

    @Serializable
    data object Home: Destination()

    @Serializable
    data class Recording(val setId: String): Destination()

    @Serializable
    data class CategoryDetails(val liftId: String): Destination()

    @Serializable
    data class CreateCategory(val liftId: String = uuid4().toString()): Destination()

    @Serializable
    data class MovementDetails(val movementId: String): Destination()

    @Serializable
    data class CreateMovement(
        val movementId: String = uuid4().toString(),
        val categoryId: String? = null
    ): Destination()

    @Serializable
    data class EditSet(
        val setId: String,
    ): Destination()

    @Serializable
    data class CreateSet(
        val categoryId: String? = null,
        val movementId: String? = null,
        @Serializable(with = InstantSerializer::class)
        val date: Instant = Clock.System.now(),
    ): Destination()

    @Serializable
    data class EditWorkout(
        val localDate: LocalDate,
    ): Destination()

    @Serializable
    data class CreateWorkout(
        val localDate: LocalDate,
    ): Destination()

    @Serializable
    data object Settings: Destination()

    @Serializable
    data class Wrapped(val year: Int): Destination()

    @Serializable
    data object Goals: Destination()
}
