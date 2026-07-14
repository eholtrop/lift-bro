package com.lift.bro.data.core.testdoubles

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.MovementId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeExerciseDataSource(
    private val exercises: List<Exercise> = emptyList()
) : ExerciseDataSource {
    var lastWorkoutId: String? = null
        private set
    var savedExercise: Exercise? = null
        private set
    var lastExerciseId: String? = null
        private set
    var lastVariationId: MovementId? = null
        private set
    var deletedId: String? = null
        private set
    var deletedVariationSetId: String? = null
        private set

    override fun get(workoutId: String): Flow<List<Exercise>> {
        lastWorkoutId = workoutId
        return flowOf(exercises)
    }

    override suspend fun save(exercise: Exercise) {
        savedExercise = exercise
    }

    override suspend fun saveVariation(exerciseId: String, variationId: MovementId) {
        lastExerciseId = exerciseId
        lastVariationId = variationId
    }

    override suspend fun delete(id: String) {
        deletedId = id
    }

    override suspend fun deleteVariation(exerciseId: String, variationId: MovementId) {
        lastExerciseId = exerciseId
        lastVariationId = variationId
    }

    override suspend fun deleteVariationSets(variationSetId: String) {
        deletedVariationSetId = variationSetId
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        lastWorkoutId = workoutId
        lastExerciseId = exerciseId
    }
}

fun fakeExerciseDataSource(
    exercises: List<Exercise> = emptyList()
): FakeExerciseDataSource = FakeExerciseDataSource(exercises)
