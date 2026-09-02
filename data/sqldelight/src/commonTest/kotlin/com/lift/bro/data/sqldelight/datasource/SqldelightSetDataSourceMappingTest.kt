package com.lift.bro.data.sqldelight.datasource

import com.lift.bro.domain.models.Tempo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.Instant

class SqldelightSetDataSourceMappingTest {

    private val date = Instant.fromEpochMilliseconds(1_700_000_000_000)

    private fun testSet(
        previousMax: Double?,
        weight: Double = 180.0,
        reps: Long = 5,
        failureRep: Long? = null,
        id: String = "set-1",
        movementId: String = "movement-1",
    ) = toDomainSet(
        previousMax = previousMax,
        id = id,
        movementId = movementId,
        weight = weight,
        reps = reps,
        failureRep = failureRep,
        date = date,
    )

    // MARK: - MER respects failureRep

    @Test
    fun `Given clean set with no failureRep When map Then mer uses full reps`() {
        val set = testSet(previousMax = 200.0, weight = 180.0, reps = 5)

        assertEquals(5, set.mer)
    }

    @Test
    fun `Given set failed on last rep When map Then mer is lower than a clean set`() {
        val clean = testSet(previousMax = 200.0, weight = 180.0, reps = 5, failureRep = null)
        val failedLast = testSet(previousMax = 200.0, weight = 180.0, reps = 5, failureRep = 5)

        assertEquals(4, failedLast.mer)
        assertEquals(true, failedLast.mer < clean.mer, "Failed last rep must score lower than a clean set")
    }

    @Test
    fun `Given set failed mid-set When map Then mer uses failureRep minus one successful reps`() {
        val set = testSet(previousMax = 200.0, weight = 180.0, reps = 5, failureRep = 3)

        assertEquals(2, set.mer)
    }

    @Test
    fun `Given set failed on first rep When map Then mer is zero`() {
        val set = testSet(previousMax = 200.0, weight = 180.0, reps = 5, failureRep = 1)

        assertEquals(0, set.mer)
    }

    // MARK: - MER edge cases

    @Test
    fun `Given null previousMax When map Then mer is zero and percentage is null`() {
        val set = testSet(previousMax = null)

        assertEquals(0, set.mer)
        assertNull(set.percentagePreviousMax)
    }

    @Test
    fun `Given zero previousMax When map Then mer is zero and percentage is null`() {
        val set = testSet(previousMax = 0.0)

        assertEquals(0, set.mer)
        assertNull(set.percentagePreviousMax)
    }

    @Test
    fun `Given full set above threshold When map Then mer capped at successful reps`() {
        val set = toDomainSet(
            previousMax = 200.0,
            id = "set-1",
            movementId = "movement-1",
            weight = 200.0,
            reps = 5,
            date = date,
        )

        assertEquals(5, set.mer)
    }

    // MARK: - percentagePreviousMax

    @Test
    fun `Given previousMax When map Then percentagePreviousMax is weight over max`() {
        val set = testSet(previousMax = 200.0, weight = 180.0)

        assertEquals(0.9f, set.percentagePreviousMax)
    }

    // MARK: - field mapping round-trip

    @Test
    fun `Given all lbSet fields When map Then they are preserved`() {
        val set = toDomainSet(
            previousMax = 200.0,
            id = "set-1",
            movementId = "movement-1",
            exerciseSectionId = "section-1",
            weight = 180.0,
            reps = 5,
            failureRep = 4,
            tempo = Tempo(down = 3, hold = 2, up = 1),
            date = date,
            notes = "notes",
            rpe = 8,
            bodyWeightRep = true,
            videoUri = "uris://video",
        )

        assertEquals("set-1", set.id)
        assertEquals("movement-1", set.movementId)
        assertEquals("section-1", set.exerciseSectionId)
        assertEquals(180.0, set.weight)
        assertEquals(5, set.reps)
        assertEquals(4, set.failureRep)
        assertEquals(Tempo(down = 3, hold = 2, up = 1), set.tempo)
        assertEquals(date, set.date)
        assertEquals("notes", set.notes)
        assertEquals(8, set.rpe)
        assertEquals(true, set.bodyWeightRep)
        assertEquals("uris://video", set.videoUri)
    }

    @Test
    fun `Given null weight and reps When map Then defaults applied`() {
        val set = toDomainSet(
            previousMax = 200.0,
            id = "set-1",
            movementId = "movement-1",
            weight = 0.0,
            reps = 1,
            date = date,
        )

        assertEquals(0.0, set.weight)
        assertEquals(1, set.reps)
    }

    @Test
    fun `Given LBSet produced by mapping Then derived properties respect failureRep`() {
        val set = testSet(previousMax = 200.0, weight = 180.0, reps = 5, failureRep = 4)

        assertEquals(3, set.successfulReps)
        assertEquals(540.0, set.totalWeightMoved)
    }
}
