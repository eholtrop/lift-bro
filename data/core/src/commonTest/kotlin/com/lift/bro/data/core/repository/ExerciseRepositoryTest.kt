package com.lift.bro.data.core.repository

import com.lift.bro.data.core.testdoubles.fakeExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.VariationSets
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseRepositoryTest {

    @Test
    fun `get delegates to data source`() = runTest {
        val exercises = listOf(
            Exercise(
                id = "e1",
                workoutId = "w1",
                variationSets = listOf(
                    VariationSets(
                        id = "vs1",
                        variation = Movement(id = "v1"),
                        sets = emptyList()
                    )
                )
            )
        )
        val dataSource = fakeExerciseDataSource(exercises = exercises)
        val repository = ExerciseRepository(dataSource)

        val result = repository.get("w1").first()

        assertEquals(exercises, result)
        assertEquals("w1", dataSource.lastWorkoutId)
    }

    @Test
    fun `save delegates to data source`() = runTest {
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            variationSets = emptyList()
        )
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.save(exercise)

        assertEquals(exercise, dataSource.savedExercise)
    }

    @Test
    fun `saveVariation delegates to data source`() = runTest {
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.saveVariation("e1", "v1")

        assertEquals("e1", dataSource.lastExerciseId)
        assertEquals("v1", dataSource.lastVariationId)
    }

    @Test
    fun `delete delegates to data source`() = runTest {
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.delete("e1")

        assertEquals("e1", dataSource.deletedId)
    }

    @Test
    fun `deleteVariation delegates to data source`() = runTest {
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.deleteVariation("e1", "v1")

        assertEquals("e1", dataSource.lastExerciseId)
        assertEquals("v1", dataSource.lastVariationId)
    }

    @Test
    fun `deleteVariationSets delegates to data source`() = runTest {
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.deleteVariationSets("vs1")

        assertEquals("vs1", dataSource.deletedVariationSetId)
    }
}
