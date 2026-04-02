package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Exercise
import kotlinx.coroutines.flow.Flow

interface IExerciseRepository {

    fun get(workoutId: String): Flow<List<Exercise>>

    // This will not save any sets stored in the exercise
    // it will only store the exercise and its related variations
    suspend fun save(exercise: Exercise)

    suspend fun saveVariation(exerciseId: String, variationId: String)

    suspend fun delete(id: String)

    suspend fun deleteVariation(exerciseId: String, variationId: String)

    suspend fun deleteVariationSets(variationSetId: String)
}
