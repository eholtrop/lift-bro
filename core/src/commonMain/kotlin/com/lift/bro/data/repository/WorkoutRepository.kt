package com.lift.bro.data.repository

import androidx.compose.animation.core.infiniteRepeatable
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.utils.debug
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

    override fun get(date: LocalDate): Flow<Workout> =
        database.workoutDataSource.getByDate(date = date).asFlow().mapToOneOrNull(dispatcher)
            .flatMapLatest { workout ->
                database.exerciseDataSource.listen(workout?.id ?: "").map { exercises ->
                    workout?.let {
                        Workout(
                            id = workout.id,
                            date = workout.date,
                            warmup = workout.warmup,
                            exercises = exercises.toList(),
                            finisher = workout.finisher,
                        )
                    } ?: Workout(
                        id = uuid4().toString(),
                        date = date,
                        exercises = emptyList(),
                    )
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