package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.repositories.IExerciseRepository
import kotlinx.coroutines.flow.Flow

class ExerciseRepository(
    private val local: ExerciseDataSource,
): IExerciseRepository {
    override fun listenAll(workoutId: String?): Flow<List<Exercise>> = local.listenAll(workoutId)

    override suspend fun save(exercise: Exercise) = local.save(exercise)

    override suspend fun delete(id: ExerciseId) = local.delete(id)

    override suspend fun save(section: Section) = local.save(section)

    override suspend fun delete(section: Section, cascading: Boolean) =
        local.delete(section = section, cascading = cascading)
}
