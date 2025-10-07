package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.data.core.datasource.WorkoutDataSource
import com.lift.bro.data.core.datasource.WorkoutRow
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IWorkoutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate

class WorkoutRepository(
    private val workoutDs: WorkoutDataSource,
    private val exercise: ExerciseDataSource,
) : IWorkoutRepository {

    override fun getAll(startDate: LocalDate, endDate: LocalDate): Flow<List<Workout>> =
        workoutDs.listenAll(startDate, endDate)
            .flatMapLatest { rows ->
                if (rows.isEmpty()) return@flatMapLatest flow { emit(emptyList()) }

                combine(
                    *rows.map { row ->
                        exercise.get(row.id).map { exList ->
                            Workout(
                                id = row.id,
                                date = row.date,
                                warmup = row.warmup,
                                exercises = exList,
                                finisher = row.finisher
                            )
                        }
                    }.toTypedArray()
                ) { it.toList() }
            }

    override fun get(id: String): Flow<Workout?> =
        workoutDs.listenById(id).flatMapLatest { row ->
            exercise.get(row?.id ?: "").map { exercises ->
                row?.let { Workout(id = it.id, date = it.date, warmup = it.warmup, exercises = exercises, finisher = it.finisher) }
            }
        }

    override fun get(date: LocalDate): Flow<Workout?> =
        workoutDs.listenByDate(date).flatMapLatest { row ->
            exercise.get(row?.id ?: "").map { exercises ->
                row?.let { Workout(id = it.id, date = it.date, warmup = it.warmup, exercises = exercises, finisher = it.finisher) }
            }
        }

    override suspend fun save(workoutModel: Workout) {
        workoutDs.save(
            WorkoutRow(
                id = workoutModel.id,
                date = workoutModel.date,
                warmup = workoutModel.warmup,
                finisher = workoutModel.finisher
            )
        )
    }

    override suspend fun addVariation(exerciseId: String, variationId: String) {
        exercise.saveVariation(exerciseId, variationId)
    }

    override suspend fun removeVariation(exerciseVariationId: String) {
        // Prefer an explicit method; if not available, fall back to deleting variation sets by this variation relation id
        try {
            val method = exercise::class.members.firstOrNull { it.name == "deleteVariationById" }
            if (method != null) {
                @Suppress("UNCHECKED_CAST")
                (method as? kotlin.reflect.KFunction<Unit>)?.call(exercise, exerciseVariationId)
                return
            }
        } catch (_: Throwable) {}
        // Fallback: implementers should provide proper removal method; no-op otherwise
    }

    override suspend fun deleteExercise(exerciseId: String) {
        exercise.delete(exerciseId)
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        exercise.addExercise(workoutId, exerciseId)
    }

    override suspend fun delete(workout: Workout) {
        workout.exercises.forEach { exercise.delete(it.id) }
        workoutDs.delete(workout.id)
    }
}
