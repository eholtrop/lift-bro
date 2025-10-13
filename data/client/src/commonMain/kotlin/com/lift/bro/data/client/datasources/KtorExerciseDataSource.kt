package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.VariationId
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class KtorExerciseDataSource(
    private val httpClient: HttpClient,
): ExerciseDataSource {

    override fun get(workoutId: String): Flow<List<Exercise>> = createConnectionFlow(httpClient, "api/ws/exercises?workoutId=$workoutId")

    override suspend fun save(exercise: Exercise) {
        TODO("Not yet implemented")
    }

    override suspend fun saveVariation(exerciseId: String, variationId: VariationId) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteVariation(exerciseId: String, variationId: VariationId) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteVariationSets(variationSetId: String) {
        TODO("Not yet implemented")
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        TODO("Not yet implemented")
    }

}
