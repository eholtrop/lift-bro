package com.lift.bro.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class LBSetTest {

    // MARK: - calculateMax tests


    @Test
    fun `Given 1 rep When calculateMax Then returns weight unchanged`() {
        val result = calculateMax(reps = 1, weight = 225.0)

        assertEquals(225.0, result)
    }

    @Test
    fun `Given reps greater than 1 When calculateMax Then returns estimated max`() {
        val result = calculateMax(reps = 5, weight = 200.0)

        // Expected: 200 * (1 + (5 / 30)) = 200 * 1.1667 = 233.33
        assertEquals(233.33, result, 0.01)
    }

    @Test
    fun `Given null reps When calculateMax Then treats as 0 reps`() {
        val result = calculateMax(reps = null, weight = 200.0)

        assertEquals(200.0, result)
    }

    @Test
    fun `Given null weight When calculateMax Then treats as 0 weight`() {
        val result = calculateMax(reps = 5L, weight = null)

        assertEquals(0.0, result)
    }

    @Test
    fun `Given both null When calculateMax Then returns 0`() {
        val result = calculateMax(reps = null, weight = null)

        assertEquals(0.0, result)
    }

    // MARK: - LBSet property tests

    @Test
    fun `Given LBSet with 1 rep When oneRepMax Then returns weight`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 225.0,
            reps = 1
        )

        assertEquals(225.0, set.oneRepMax)
    }

    @Test
    fun `Given LBSet with reps greater than 1 When oneRepMax Then returns null`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 200.0,
            reps = 5
        )

        assertNull(set.oneRepMax)
    }

    @Test
    fun `Given LBSet When estimateMax Then calculates correctly`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 200.0,
            reps = 5
        )

        val result = set.estimateMax

        assertNotNull(result)
        assertEquals(233.33, result, 0.01)
    }

    @Test
    fun `Given LBSet with failureRep When estimateMax Then failure rep respected`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 200.0,
            reps = 5,
            failureRep = 4
        )

        val result = set.estimateMax

        assertNotNull(result)
        assertEquals(220.0, result, 0.01)
    }

    @Test
    fun `Given LBSet with 1 rep When estimateMax Then applies formula`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 225.0,
            reps = 1
        )

        val result = set.estimateMax

        // Even for 1 rep, estimateMax applies formula: 225 * (1 + 1/30) = 232.5
        assertNotNull(result)
        assertEquals(232.5, result, 0.01)
    }

    @Test
    fun `Given LBSet When totalWeightMoved Then returns weight times reps`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 50.0,
            reps = 10
        )

        assertEquals(500.0, set.totalWeightMoved)
    }

    @Test
    fun `Given LBSet has a failureRep Then TWM move respects it`() {
        val set = LBSet(
            id = "test",
            reps = 3,
            failureRep = 3,
            weight = 2.0,
            movementId = "",
        )

        assertEquals(4.0, set.totalWeightMoved)
    }

    @Test
    fun `Given LBSet with zero weight When totalWeightMoved Then returns 0`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 0.0,
            reps = 10
        )

        assertEquals(0.0, set.totalWeightMoved)
    }

    // MARK: - Extension property formatting tests

    @Test
    fun `Given LBSet When formattedMax Then returns correct format`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            weight = 100.0,
            reps = 5,
            tempo = Tempo(down = 3, hold = 1, up = 1)
        )

        assertEquals("5 x 3/1/1", set.formattedMax)
    }

    @Test
    fun `Given LBSet When formattedTempo Then returns correct format`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            tempo = Tempo(down = 4, hold = 2, up = 1)
        )

        assertEquals("4/2/1", set.formattedTempo)
    }

    @Test
    fun `Given LBSet When formattedReps Then returns correct format`() {
        val set = LBSet(
            id = "test",
            movementId = "var1",
            reps = 8,
            tempo = Tempo(down = 3, hold = 1, up = 1)
        )

        assertEquals("3/1/1 x 8", set.formattedReps)
    }
}
