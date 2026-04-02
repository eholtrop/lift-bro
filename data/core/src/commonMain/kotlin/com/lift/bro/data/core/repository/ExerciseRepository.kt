package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.repositories.IExerciseRepository
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val local: ExerciseDataSource
) : IExerciseRepository {
    override fun get(workoutId: String): Flow<List<Exercise>> = local.get(workoutId)
    override suspend fun save(exercise: Exercise) = local.save(exercise)
    override suspend fun saveVariation(
        exerciseId: String,
        variationId: String
    ) = local.saveVariation(exerciseId, variationId)
    override suspend fun delete(id: String) = local.delete(id)
    override suspend fun deleteVariation(
        exerciseId: String,
        variationId: String
    ) = local.deleteVariation(exerciseId, variationId)
    override suspend fun deleteVariationSets(variationSetId: String) = local.deleteVariationSets(variationSetId)
}
