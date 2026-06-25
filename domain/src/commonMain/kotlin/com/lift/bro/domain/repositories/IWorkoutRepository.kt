package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface IWorkoutRepository {

    fun getAll(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Long = Long.MAX_VALUE,
    ): Flow<List<Workout>>

    fun get(date: LocalDate): Flow<Workout?>

    suspend fun save(workout: Workout)

    suspend fun delete(workout: Workout)
}
