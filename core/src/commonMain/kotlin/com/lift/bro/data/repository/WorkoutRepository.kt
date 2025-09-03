package com.lift.bro.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class WorkoutRepository(
    private val database: LBDatabase = dependencies.database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): IWorkoutRepository {

    override fun getAll(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Flow<List<Workout>> = combine(
        database.workoutDataSource.getAll(startDate = startDate, endDate = endDate).asFlow().mapToList(dispatcher),
        database.setDataSource.listenAll(startDate = startDate, endDate = endDate),
        database.variantDataSource.listenAll(),
    ) { workouts, sets, variations ->
        val dateToWorkoutSetMap: MutableMap<LocalDate, Pair<List<LBSet>?, comliftbrodb.Workout?>> =
            mutableMapOf()
        sets.groupBy { it.date.toLocalDate() }.forEach {
            dateToWorkoutSetMap[it.key] = it.value to null
        }
        workouts.associateBy { it.date }.map {
            dateToWorkoutSetMap[it.key] = dateToWorkoutSetMap[it.key]?.first to it.value
        }

        dateToWorkoutSetMap.map { (date, pair) ->
            val workout = pair.second ?: comliftbrodb.Workout(
                id = uuid4().toString(),
                finisher = null,
                warmup = null,
                date = date,
            )
            val sets = pair.first ?: emptyList()

            WorkoutConverter.toDomain(
                workout = workout,
                sets = sets.filter { it.date.toLocalDate() == workout.date },
                variations = variations,
            )
        }
    }

    override fun get(id: String): Flow<Workout?> {
        TODO("Not yet implemented")
    }

    override fun get(date: LocalDate): Flow<Workout> = combine(
        database.workoutDataSource.getByDate(date = date).asFlow().mapToOneOrNull(dispatcher),
        database.setDataSource.listenAll(startDate = date, endDate = date),
        database.variantDataSource.listenAll()
    ) { workout, sets, variations ->
        WorkoutConverter.toDomain(
            workout = workout ?: comliftbrodb.Workout(
                id = uuid4().toString(),
                finisher = null,
                warmup = null,
                date = date,
            ),
            sets = sets,
            variations = variations,
        )
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
}

object WorkoutConverter {
    fun toDomain(
        workout: comliftbrodb.Workout,
        sets: List<LBSet>,
        variations: List<Variation>,
    ): Workout = Workout(
        id = workout.id,
        finisher = workout.finisher,
        warmup = workout.warmup,
        date = workout.date,
        exercises = sets.groupBy { it.variationId }.map { (variationId, sets) ->
            variations.firstOrNull { it.id == variationId }?.let {
                ExerciseConverter.toDomain(it, sets)
            }
        }.filterNotNull()
    )
}

object ExerciseConverter {
    fun toDomain(variation: Variation, sets: List<LBSet>): Exercise = Exercise(
        variation = variation,
        sets = sets.filter { it.variationId == variation.id },
    )
}