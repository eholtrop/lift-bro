package com.lift.bro.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseTest {

    @Test
    fun `Given exercise with one variation When total weight moved Then sums all sets`() {
        val set1 = LBSet(id = "1", variationId = "v1", weight = 100.0, reps = 5)
        val set2 = LBSet(id = "2", variationId = "v1", weight = 100.0, reps = 5)
        val set3 = LBSet(id = "3", variationId = "v1", weight = 100.0, reps = 5)
        val variation = Movement(id = "v1")
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = listOf(
                VariationSets(id = "vs1", variation = variation, sets = listOf(set1, set2, set3))
            )
        )

        // 100*5 + 100*5 + 100*5 = 1500
        assertEquals(1500.0, exercise.totalWeightMoved, 0.001)
    }

    @Test
    fun `Given exercise with multiple variations When total weight moved Then sums across all variations`() {
        val variation1 = Movement(id = "v1")
        val variation2 = Movement(id = "v2")
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = listOf(
                VariationSets(
                    id = "vs1",
                    variation = variation1,
                    sets = listOf(
                        LBSet(id = "1", variationId = "v1", weight = 100.0, reps = 5)
                    )
                ),
                VariationSets(
                    id = "vs2",
                    variation = variation2,
                    sets = listOf(
                        LBSet(id = "2", variationId = "v2", weight = 80.0, reps = 8)
                    )
                )
            )
        )

        // v1: 100*5 = 500, v2: 80*8 = 640
        assertEquals(1140.0, exercise.totalWeightMoved, 0.001)
    }

    @Test
    fun `Given exercise with no sets When total weight moved Then returns 0`() {
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = emptyList()
        )

        assertEquals(0.0, exercise.totalWeightMoved, 0.001)
    }

    @Test
    fun `Given exercise with empty variation sets When total weight moved Then returns 0`() {
        val variation = Movement(id = "v1")
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = listOf(
                VariationSets(id = "vs1", variation = variation, sets = emptyList())
            )
        )

        assertEquals(0.0, exercise.totalWeightMoved, 0.001)
    }

    @Test
    fun `Given exercise with zero weight sets When total weight moved Then returns 0`() {
        val variation = Movement(id = "v1")
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = listOf(
                VariationSets(
                    id = "vs1",
                    variation = variation,
                    sets = listOf(
                        LBSet(id = "1", variationId = "v1", weight = 0.0, reps = 10)
                    )
                )
            )
        )

        assertEquals(0.0, exercise.totalWeightMoved, 0.001)
    }
}
