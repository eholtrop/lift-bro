package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.VariationId
import kotlinx.coroutines.flow.Flow

interface ExerciseDataSource {
    fun get(workoutId: String): Flow<List<Exercise>>
    suspend fun save(exercise: Exercise)
    suspend fun saveVariation(exerciseId: String, variationId: VariationId)
    suspend fun delete(id: String)
    suspend fun deleteVariation(exerciseId: String, variationId: VariationId)
    suspend fun deleteVariationSets(variationSetId: String)
    suspend fun addExercise(workoutId: String, exerciseId: String)
}
