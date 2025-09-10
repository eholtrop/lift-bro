package com.lift.bro.data.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.benasher44.uuid.uuid4
import com.lift.bro.data.buildLBSet
import com.lift.bro.data.toDomain
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.models.VariationSets
import com.lift.bro.utils.toLocalDate
import comliftbrodb.ExerciseQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

interface ExerciseDataSource {

    fun listen(
        workoutId: String,
    ): Flow<List<Exercise>>


    suspend fun save(exercise: Exercise)

    suspend fun delete(id: String)

    suspend fun addVariation(exerciseId: String, variationId: VariationId)

    suspend fun removeVariation(exerciseId: String, variationId: VariationId)

    suspend fun removeVariaiton(exerciseVariationId: String)
}

class LBExerciseDataSource(
    private val exerciseQueries: ExerciseQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ExerciseDataSource {

    override fun listen(
        workoutId: String,
    ): Flow<List<Exercise>> = combine(
        exerciseQueries.getByWorkoutId(workoutId).flowToList(dispatcher),
        exerciseQueries.getExerciseVariationsByWorkoutId(workoutId).flowToList(dispatcher),
        setQueries.getByWorkoutId(workoutId = workoutId, limit = Long.MAX_VALUE)
            .flowToList(dispatcher),
        variationQueries.getAll().flowToList(dispatcher).flatMapLatest { variations ->
            combine(
                *variations.map { variation ->
                    combine(
                        setQueries.getOneRepMaxForVariation(variation.id, Instant.DISTANT_FUTURE).flowToOneOrNull(),
                        setQueries.getEMaxForVariation(variation.id, Instant.DISTANT_FUTURE).flowToOneOrNull(),
                        setQueries.getMaxRepsForVariation(variation.id, Instant.DISTANT_FUTURE).flowToOneOrNull(),
                    ) { orm, volume, reps ->
                        Variation(
                            id = variation.id,
                            name = variation.name,
                            notes = variation.notes,
                            favourite = variation.favourite == 1L,
                            lift = Lift(
                                id = variation.lift_id,
                                color = variation.lift_color?.toULong(),
                                name = variation.lift_name,
                            ),
                            oneRepMax = orm?.toDomain(),
                            eMax = volume?.toDomain(),
                            maxReps = reps?.toDomain(),
                        )
                    }
                }.toTypedArray()
            ) { it.toList() }
        },
    ) { exercises, exerciseVariations, sets, variations ->
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
                                .filter { it.date.toLocalDate() == it.date_ } // date_ is the workout date...
                                .map {
                                    LBSet(
                                        id = it.id,
                                        variationId = it.variationId,
                                        weight = it.weight ?: 0.0,
                                        reps = it.reps ?: 1,
                                        date = it.date,
                                        notes = it.notes,
                                        rpe = it.rpe?.toInt(),
                                        tempo = com.lift.bro.domain.models.Tempo(
                                            down = it.tempoDown ?: 3,
                                            hold = it.tempoHold ?: 1,
                                            up = it.tempoUp ?: 1,
                                        )
                                    )
                                }
                        )
                    }
            )
        }
    }

    override suspend fun save(exercise: Exercise) {
        withContext(dispatcher) {
            exerciseQueries.save(
                id = exercise.id,
                workoutId = exercise.workoutId,
            )

            exercise.variationSets.forEach { vSet ->
                exerciseQueries.saveVariation(
                    id = vSet.id,
                    exerciseId = exercise.id,
                    varationId = vSet.variation.id,
                )
            }
        }
    }

    override suspend fun delete(id: String) {
        withContext(dispatcher) {
            exerciseQueries.delete(id)
            exerciseQueries.deleteVariationsByExercise(id)
        }
    }

    override suspend fun addVariation(
        exerciseId: String,
        variationId: VariationId,
    ) {
        withContext(dispatcher) {
            exerciseQueries.saveVariation(
                id = uuid4().toString(),
                exerciseId = exerciseId,
                varationId = variationId,
            )

        }
    }

    override suspend fun removeVariation(
        exerciseId: String,
        variationId: VariationId,
    ) {
        withContext(dispatcher) {
            exerciseQueries.deleteVariationBy(exerciseId, variationId)
        }
    }

    override suspend fun removeVariaiton(exerciseVariationId: String) {
        withContext(dispatcher) {
            exerciseQueries.deleteVariationsById(exerciseVariationId)
        }
    }

}