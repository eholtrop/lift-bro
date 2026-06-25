package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.Section
import kotlinx.coroutines.flow.Flow

interface ExerciseDataSource {
    fun listenAll(workoutId: String?): Flow<List<Exercise>>
    suspend fun save(exercise: Exercise)
    suspend fun delete(id: ExerciseId)

    suspend fun save(section: Section)
    suspend fun delete(section: Section, cascading: Boolean)
}
