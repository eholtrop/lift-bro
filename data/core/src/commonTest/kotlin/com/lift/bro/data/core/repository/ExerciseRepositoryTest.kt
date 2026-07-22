package com.lift.bro.data.core.repository

import com.lift.bro.data.core.testdoubles.fakeExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Section
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExerciseRepositoryTest {

    @Test
    fun `listenAll delegates to data source`() = runTest {
        val exercises = listOf(
            Exercise(
                id = "e1",
                workoutId = "w1",
                sections = listOf(
                    Section(id = "vs1", exerciseId = "e1", sets = emptyList())
                )
            )
        )
        val dataSource = fakeExerciseDataSource(exercises = exercises)
        val repository = ExerciseRepository(dataSource)

        val result = repository.listenAll("w1").first()

        assertEquals(exercises, result)
        assertEquals("w1", dataSource.lastWorkoutId)
    }

    @Test
    fun `save delegates to data source`() = runTest {
        val exercise = Exercise(
            id = "e1",
            workoutId = "w1",
            sections = emptyList()
        )
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.save(exercise)

        assertEquals(exercise, dataSource.savedExercise)
    }

    @Test
    fun `delete delegates to data source`() = runTest {
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.delete("e1")

        assertEquals("e1", dataSource.deletedId)
    }

    @Test
    fun `save section delegates to data source`() = runTest {
        val section = Section(id = "s1", exerciseId = "e1", sets = emptyList())
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.save(section)

        assertEquals(section, dataSource.savedSection)
    }

    @Test
    fun `delete section delegates to data source`() = runTest {
        val section = Section(id = "s1", exerciseId = "e1", sets = emptyList())
        val dataSource = fakeExerciseDataSource()
        val repository = ExerciseRepository(dataSource)

        repository.delete(section, cascading = true)

        assertEquals(section, dataSource.lastDeletedSection)
        assertEquals(true, dataSource.lastDeletedSectionCascading)
    }
}
