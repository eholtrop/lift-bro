package com.lift.bro.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.di.dependencies
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
    private val database: LBDatabase = dependencies.database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): IWorkoutRepository {

    override fun getAll(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<Workout>> = database.workoutDataSource.getAll(startDate = startDate, endDate = endDate).flowToList(dispatcher)
            .flatMapLatest { workouts ->
                if (workouts.isEmpty()) return@flatMapLatest flow { emit(emptyList()) }

                combine(
                    *workouts.map { workout ->
                        database.exerciseDataSource.listen(workout.id).map {
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

    override fun get(id: String): Flow<Workout?> {
        TODO("Not yet implemented")
    }

    override fun get(date: LocalDate): Flow<Workout?> =
        database.workoutDataSource.getByDate(date = date).flowToOneOrNull()
            .flatMapLatest { workout ->
                when (workout) {
                    null -> flow<Workout?> { emit(null) }
                    else -> database.exerciseDataSource.listen(workout.id).map { exercises ->
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
        }
    }

    override suspend fun addVariation(
        exerciseId: ExerciseId,
        variationId: VariationId
    ) {
        database.exerciseDataSource.addVariation(
            exerciseId = exerciseId,
            variationId = variationId,
        )
    }

    override suspend fun removeVariation(exerciseVariationId: String) {
        database.exerciseDataSource.removeVariaiton(exerciseVariationId)
    }

    override suspend fun deleteExercise(exerciseId: String) {
        database.exerciseDataSource.delete(exerciseId)
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        database.exerciseDataSource.addExercise(
            workoutId = workoutId,
            exerciseId = exerciseId,
        )
    }

    override suspend fun delete(workout: Workout) {
        withContext(dispatcher) {
            database.workoutDataSource.delete(workout.id)
            workout.exercises.forEach {
                database.exerciseDataSource.delete(it.id)
                database.exerciseDataSource.delete(it.id)
            }
        }

    }
}