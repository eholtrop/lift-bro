package com.lift.bro.data.repository

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class WorkoutRepository(
    private val database: LBDatabase = dependencies.database,
    private val exerciseDataSource: ExerciseDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IWorkoutRepository {

    override fun getAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Long,
    ): Flow<List<Workout>> =
        database.workoutDataSource.getAll(startDate = startDate, endDate = endDate, limit = limit)
            .flowToList(dispatcher)
            .flatMapLatest { workouts ->
                if (workouts.isEmpty()) return@flatMapLatest flow { emit(emptyList()) }

                combine(
                    *workouts.map { workout ->
                        exerciseDataSource.listenAll(workout.id).map {
                            Workout(
                                id = workout.id,
                                date = workout.date,
                                warmup = workout.warmup,
                                exercises = it.toList(),
                                finisher = workout.finisher
                            )
                        }
                    }.toTypedArray()
                ) { it.toList() }
            }

    override fun get(date: LocalDate): Flow<Workout?> =
        database.workoutDataSource.getByDate(date = date).flowToOneOrNull()
            .flatMapLatest { workout ->
                exerciseDataSource.listenAll(workout?.id ?: "").map { exercises ->
                    workout?.let {
                        Workout(
                            id = workout.id,
                            date = workout.date,
                            warmup = workout.warmup,
                            exercises = exercises.toList(),
                            finisher = workout.finisher,
                        )
                    }
                }
            }

    override suspend fun save(workout: Workout) {
        withContext(dispatcher) {
            database.workoutDataSource.save(
                id = workout.id,
                finisher = workout.finisher,
                warmup = workout.warmup,
                date = workout.date,
            )
            workout.exercises.forEach { exercise ->
                exerciseDataSource.save(exercise)
                exercise.sections.forEach { section ->
                    exerciseDataSource.save(section)
                }
            }
        }
    }

    override suspend fun delete(workout: Workout) {
        withContext(dispatcher) {
            database.workoutDataSource.delete(workout.id)
            workout.exercises.forEach {
                exerciseDataSource.delete(it.id)
            }
        }
    }
}
