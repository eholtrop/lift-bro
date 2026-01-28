package com.lift.bro.presentation.set

import com.lift.bro.domain.models.Variation
import com.lift.bro.`ktx-datetime`.toLocalDate
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class EditSetReducerTest {

    @Test
    fun `Given state When WeightChanged Then updates weight`() = runTest {
        // Given
        val state = EditSetState(id = "1", weight = 100.0)
        val event = EditSetEvent.WeightChanged(225.0)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(225.0, result.weight)
    }

    @Test
    fun `Given null state When WeightChanged Then returns null`() = runTest {
        // Given
        val state: EditSetState? = null
        val event = EditSetEvent.WeightChanged(225.0)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNull(result)
    }

    @Test
    fun `Given state When RepChanged Then updates reps`() = runTest {
        // Given
        val state = EditSetState(id = "1", reps = 5)
        val event = EditSetEvent.RepChanged(10)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(10L, result.reps)
    }

    @Test
    fun `Given state When RpeChanged Then updates rpe`() = runTest {
        // Given
        val state = EditSetState(id = "1", rpe = 7)
        val event = EditSetEvent.RpeChanged(9)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(9, result.rpe)
    }

    @Test
    fun `Given state When EccChanged Then updates eccentric`() = runTest {
        // Given
        val state = EditSetState(id = "1", tempo = TempoState(ecc = 3))
        val event = EditSetEvent.TempoChanged(TempoState(ecc = 4))

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(4L, result.tempo.ecc)
    }

    @Test
    fun `Given state When IsoChanged Then updates isometric`() = runTest {
        // Given
        val state = EditSetState(id = "1", tempo = TempoState(iso = 1))
        val event = EditSetEvent.TempoChanged(TempoState(iso = 2))

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(2L, result.tempo.iso)
    }

    @Test
    fun `Given state When ConChanged Then updates concentric`() = runTest {
        // Given
        val state = EditSetState(id = "1", tempo = TempoState(con = 1))
        val event = EditSetEvent.TempoChanged(TempoState(con = 3))

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(3L, result.tempo.con)
    }

    @Test
    fun `Given state When NotesChanged Then updates notes`() = runTest {
        // Given
        val state = EditSetState(id = "1", notes = "Old notes")
        val event = EditSetEvent.NotesChanged("New notes")

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals("New notes", result.notes)
    }

    @Test
    fun `Given state When VariationSelected Then updates variation`() = runTest {
        // Given
        val state = EditSetState(id = "1")
        val event = EditSetEvent.VariationSelected(Variation("variation-123"))

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertNotNull(result.variation)
        assertEquals("variation-123", result.variation.variation.id)
    }

    @Test
    fun `Given state When DateSelected Then updates date`() = runTest {
        // Given
        val oldDate = Clock.System.now()
        val newDate = LocalDate(2023, 1, 1)
        val state = EditSetState(id = "1", date = oldDate)
        val event = EditSetEvent.DateSelected(newDate)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(newDate, result.date.toLocalDate())
    }

    @Test
    fun `Given state When DeleteSetClicked Then returns state unchanged`() = runTest {
        // Given - DeleteSetClicked is handled by side effects
        val state = EditSetState(id = "1", weight = 100.0, reps = 5)
        val event = EditSetEvent.DeleteSetClicked

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertEquals(state, result)
    }

    @Test
    fun `Given state with multiple fields When weight changed Then preserves other fields`() = runTest {
        // Given
        val variation = Variation(id = "v1", name = "Squat")
        val state = EditSetState(
            id = "1",
            variation = SetVariation(variation),
            weight = 100.0,
            reps = 5,
            tempo = TempoState(3, 1, 1),
            notes = "Test notes",
            rpe = 8
        )
        val event = EditSetEvent.WeightChanged(225.0)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertEquals(225.0, result.weight)
        assertEquals(variation, result.variation?.variation)
        assertEquals(5L, result.reps)
        assertEquals(3L, result.tempo.ecc)
        assertEquals(1L, result.tempo.iso)
        assertEquals(1L, result.tempo.con)
        assertEquals("Test notes", result.notes)
        assertEquals(8, result.rpe)
    }

    @Test
    fun `Given state When RepChanged to null Then updates reps to null`() = runTest {
        // Given
        val state = EditSetState(id = "1", reps = 5)
        val event = EditSetEvent.RepChanged(null)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertNull(result.reps)
    }

    @Test
    fun `Given state When WeightChanged to null Then updates weight to null`() = runTest {
        // Given
        val state = EditSetState(id = "1", weight = 100.0)
        val event = EditSetEvent.WeightChanged(null)

        // When
        val result = EditSetReducer(state, event)

        // Then
        assertNotNull(result)
        assertNull(result.weight)
    }
}
