package com.lift.bro.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.benasher44.uuid.uuid4
import com.lift.bro.data.LBDatabase
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.domain.models.Workout
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
import com.lift.bro.utils.debug
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.toLocalDate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.LocalDate

class ExerciseRepository(
    private val database: LBDatabase = dependencies.database,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): IExerciseRepository {

    override fun get(workoutId: String): Flow<List<Exercise>> = combine(
        database.exerciseQueries.getByWorkoutId(workoutId).asFlow().mapToList(dispatcher),
        database.exerciseQueries.getExerciseVariationsByWorkoutId(workoutId).asFlow()
            .mapToList(dispatcher),
        database.workoutDataSource.get(workoutId).asFlow().mapToOneOrNull(dispatcher),
        database.setDataSource.listenAll(),
        database.variantDataSource.listenAll(),
    ) { exercises, exerciseVariations, workout, sets, variations ->
        exercises.map { exercise ->
            Exercise(
                id = exercise.id,
                workoutId = workoutId,
                variationSets = exerciseVariations.filter { it.exerciseId == exercise.id }
                    .map { ev ->
                        VariationSets(
                            id = ev.id,
                            variation = variations.first { it.id == ev.varationId },
                            sets = sets.filter { it.variationId == ev.varationId }
                                .filter {
                                    workout?.date == it.date.toLocalDate()
                                }
                        )
                    }
            )
        }
    }

    override suspend fun save(exercise: Exercise) {
        withContext(Dispatchers.IO) {
            database.exerciseQueries.save(
                id = exercise.id,
                workoutId = exercise.workoutId,
            )

            exercise.variationSets.forEach { vSet ->
                database.exerciseQueries.saveVariation(
                    id = vSet.id,
                    exerciseId = exercise.id,
                    varationId = vSet.variation.id,
                )
            }
        }
    }

    override suspend fun delete(id: String) {
        withContext(Dispatchers.IO) {
            database.exerciseQueries.delete(id)
            database.exerciseQueries.deleteVariationsByExercise(id)
        }
    }

    override suspend fun deleteVariation(exerciseId: String, variationId: String) {
        withContext(Dispatchers.IO) {
            database.exerciseQueries.deleteVariationBy(exerciseId, variationId)
        }
    }

    override suspend fun deleteVariationSets(variationSetId: String) {
        withContext(Dispatchers.IO) {
            database.exerciseQueries.deleteVariationsById(variationSetId)
        }
    }
}