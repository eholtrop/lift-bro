package com.lift.bro.data.repository

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.data.sqldelight.datasource.asFlowList
import com.lift.bro.data.sqldelight.datasource.asFlowOneOrNull
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.VariationId
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
    private val database: LBDatabase,
    private val exerciseDataSource: ExerciseDataSource,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : IWorkoutRepository {

    override fun getAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Long,
    ): Flow<List<Workout>> =
        database.workoutQueries.getAll(startDate = startDate, endDate = endDate, limit = limit)
            .asFlowList(dispatcher)
            .flatMapLatest { workouts ->
                if (workouts.isEmpty()) return@flatMapLatest flow { emit(emptyList()) }

                combine(
                    *workouts.map { workout ->
                        exerciseDataSource.get(workout.id).map {
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
        database.workoutQueries.getByDate(date = date).asFlowOneOrNull(dispatcher)
            .flatMapLatest { workout ->
                exerciseDataSource.get(workout?.id ?: "").map { exercises ->
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
            database.workoutQueries.save(
                id = workout.id,
                finisher = workout.finisher,
                warmup = workout.warmup,
                date = workout.date,
            )
        }
    }

    override suspend fun addVariation(
        exerciseId: ExerciseId,
        variationId: VariationId,
    ) {
        exerciseDataSource.saveVariation(
            exerciseId = exerciseId,
            variationId = variationId,
        )
    }

    override suspend fun removeVariation(exerciseVariationId: String) {
        exerciseDataSource.deleteVariationSets(exerciseVariationId)
    }

    override suspend fun deleteExercise(exerciseId: String) {
        exerciseDataSource.delete(exerciseId)
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        exerciseDataSource.addExercise(
            workoutId = workoutId,
            exerciseId = exerciseId,
        )
    }

    override suspend fun delete(workout: Workout) {
        withContext(dispatcher) {
            database.workoutQueries.delete(workout.id)
            workout.exercises.forEach {
                exerciseDataSource.delete(it.id)
                exerciseDataSource.delete(it.id)
            }
        }
    }

    override suspend fun deleteAll() {
        exerciseDataSource.deleteAll()
        database.workoutQueries.deleteAll()
    }
}
