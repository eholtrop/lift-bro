package com.lift.bro.presentation.lift

import com.lift.bro.domain.models.Variation
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EditLiftReducerTest {

    @Test
    fun `Given state When NameChanged Then updates name`() = runTest {
        // Given
        val state = EditLiftState(id = "1", name = "Old Name")
        val event = EditLiftEvent.NameChanged("New Name")

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals("New Name", result.name)
        assertEquals("1", result.id)
    }

    @Test
    fun `Given null state When NameChanged Then returns null`() = runTest {
        // Given
        val state: EditLiftState? = null
        val event = EditLiftEvent.NameChanged("New Name")

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given state with variations When VariationNameChanged Then updates matching variation`() = runTest {
        // Given
        val variation1 = Variation(id = "v1", name = "Original Name 1")
        val variation2 = Variation(id = "v2", name = "Original Name 2")
        val state = EditLiftState(
            id = "1",
            name = "Lift",
            variations = listOf(variation1, variation2)
        )
        val event = EditLiftEvent.VariationNameChanged(variation1, "Updated Name 1")

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(2, result.variations.size)
        assertEquals("Updated Name 1", result.variations[0].name)
        assertEquals("Original Name 2", result.variations[1].name)
    }

    @Test
    fun `Given state When VariationNameChanged with non-existent variation Then returns state unchanged`() = runTest {
        // Given
        val variation1 = Variation(id = "v1", name = "Name 1")
        val nonExistentVariation = Variation(id = "non-existent", name = "Non Existent")
        val state = EditLiftState(
            id = "1",
            name = "Lift",
            variations = listOf(variation1)
        )
        val event = EditLiftEvent.VariationNameChanged(nonExistentVariation, "New Name")

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(1, result.variations.size)
        assertEquals("Name 1", result.variations[0].name)
    }

    @Test
    fun `Given state When AddVariation Then adds new variation to start of list`() = runTest {
        // Given
        val existingVariation = Variation(id = "v1", name = "Existing")
        val state = EditLiftState(
            id = "1",
            name = "Lift",
            variations = listOf(existingVariation)
        )
        val event = EditLiftEvent.AddVariation

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(2, result.variations.size)
        // New variation should be first with default values
        assertEquals("Existing", result.variations[1].name)
    }

    @Test
    fun `Given empty variations When AddVariation Then adds single variation`() = runTest {
        // Given
        val state = EditLiftState(id = "1", name = "Lift", variations = emptyList())
        val event = EditLiftEvent.AddVariation

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(1, result.variations.size)
    }

    @Test
    fun `Given state with variations When VariationRemoved Then removes matching variation`() = runTest {
        // Given
        val variation1 = Variation(id = "v1", name = "Variation 1")
        val variation2 = Variation(id = "v2", name = "Variation 2")
        val variation3 = Variation(id = "v3", name = "Variation 3")
        val state = EditLiftState(
            id = "1",
            name = "Lift",
            variations = listOf(variation1, variation2, variation3)
        )
        val event = EditLiftEvent.VariationRemoved(variation2)

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(2, result.variations.size)
        assertEquals("Variation 1", result.variations[0].name)
        assertEquals("Variation 3", result.variations[1].name)
    }

    @Test
    fun `Given state When VariationRemoved with non-existent variation Then returns state unchanged`() = runTest {
        // Given
        val variation1 = Variation(id = "v1", name = "Variation 1")
        val nonExistentVariation = Variation(id = "non-existent", name = "Non Existent")
        val state = EditLiftState(
            id = "1",
            name = "Lift",
            variations = listOf(variation1)
        )
        val event = EditLiftEvent.VariationRemoved(nonExistentVariation)

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(1, result.variations.size)
        assertEquals("Variation 1", result.variations[0].name)
    }

    @Test
    fun `Given state When DeleteLift Then returns state unchanged`() = runTest {
        // Given - DeleteLift is handled by side effects, not reducer
        val state = EditLiftState(id = "1", name = "Lift")
        val event = EditLiftEvent.DeleteLift

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given state with color When name changed Then preserves color`() = runTest {
        // Given
        val state = EditLiftState(
            id = "1",
            name = "Old Name",
            liftColor = LiftColor(255, 100, 150, 200)
        )
        val event = EditLiftEvent.NameChanged("New Name")

        // When
        val result = EditLiftReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals("New Name", result.name)
        assertEquals(LiftColor(255, 100, 150, 200), result.liftColor)
    }
}
