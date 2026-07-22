package com.lift.bro.data.core.testdoubles

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExerciseDataSource(
    private val exercises: List<Exercise> = emptyList()
) : ExerciseDataSource {
    var lastWorkoutId: String? = null
        private set
    var savedExercise: Exercise? = null
        private set
    var deletedId: String? = null
        private set
    var savedSection: Section? = null
        private set
    var lastDeletedSection: Section? = null
        private set
    var lastDeletedSectionCascading: Boolean? = null
        private set

    override fun listenAll(workoutId: String?): Flow<List<Exercise>> {
        lastWorkoutId = workoutId
        return flowOf(exercises)
    }

    override suspend fun save(exercise: Exercise) {
        savedExercise = exercise
    }

    override suspend fun delete(id: ExerciseId) {
        deletedId = id
    }

    override suspend fun save(section: Section) {
        savedSection = section
    }

    override suspend fun delete(section: Section, cascading: Boolean) {
        lastDeletedSection = section
        lastDeletedSectionCascading = cascading
    }
}

fun fakeExerciseDataSource(
    exercises: List<Exercise> = emptyList()
): FakeExerciseDataSource = FakeExerciseDataSource(exercises)
