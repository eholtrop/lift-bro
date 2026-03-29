package com.lift.bro.data.repository

import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate

@Suppress("EmptyFunctionBlock")
class WorkoutRepository(
    private val database: LBDatabase,
) : IWorkoutRepository {

    override fun getAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Long,
    ): Flow<List<Workout>> = flow { emit(emptyList()) }

    override fun get(id: String): Flow<Workout?> = flow { emit(null) }

    override fun get(date: LocalDate): Flow<Workout?> = flow { emit(null) }

    override suspend fun save(workout: Workout) {}

    override suspend fun addVariation(exerciseId: ExerciseId, variationId: VariationId) {}

    override suspend fun removeVariation(exerciseVariationId: String) {}

    override suspend fun deleteExercise(exerciseId: String) {}

    override suspend fun addExercise(workoutId: String, exerciseId: String) {}

    override suspend fun delete(workout: Workout) {}

    override suspend fun deleteAll() {
        database.workoutQueries.deleteAll()
    }
}
