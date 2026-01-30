package com.lift.bro.data.sqldelight.datasource

import com.benasher44.uuid.uuid4
import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.models.VariationSets
import comliftbrodb.ExerciseQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant
import tv.dpal.ktx.datetime.toLocalDate

class SqldelightExerciseDataSource(
    private val exerciseQueries: ExerciseQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ExerciseDataSource {

    override fun get(workoutId: String): Flow<List<Exercise>> = combine(
        exerciseQueries.getByWorkoutId(workoutId).asFlowList(dispatcher),
        exerciseQueries.getExerciseVariationsByWorkoutId(workoutId).asFlowList(dispatcher)
            .flatMapLatest { variationSets ->
                if (variationSets.isEmpty()) return@flatMapLatest flow { emit(emptyList()) }
                combine(
                    *variationSets.map { exercise ->
                        combine(
                            setQueries.getOneRepMaxForVariation(
                                exercise.variation_id,
                                Instant.DISTANT_FUTURE
                            ).asFlowOneOrNull(dispatcher),
                            setQueries.getEMaxForVariation(
                                exercise.variation_id,
                                Instant.DISTANT_FUTURE
                            ).asFlowOneOrNull(dispatcher),
                            setQueries.getMaxRepsForVariation(
                                exercise.variation_id,
                                Instant.DISTANT_FUTURE
                            ).asFlowOneOrNull(dispatcher),
                        ) { orm, volume, reps ->
                            Triple(
                                exercise.exercise_variation_id,
                                exercise.exercise_id,
                                Variation(
                                    id = exercise.variation_id,
                                    name = exercise.variation_name,
                                    notes = exercise.variation_notes,
                                    favourite = exercise.variation_is_favourite == 1L,
                                    lift = Lift(
                                        id = exercise.lift_id,
                                        color = exercise.lift_color?.toULong(),
                                        name = exercise.lift_name,
                                    ),
                                    oneRepMax = orm?.toDomain()
                                        ?.copy(bodyWeightRep = exercise.variation_is_body_weight?.let { it == 1L }),
                                    eMax = volume?.toDomain()
                                        ?.copy(bodyWeightRep = exercise.variation_is_body_weight?.let { it == 1L }),
                                    maxReps = reps?.toDomain()
                                        ?.copy(bodyWeightRep = exercise.variation_is_body_weight?.let { it == 1L }),
                                    bodyWeight = exercise.variation_is_body_weight == 1L,
                                )
                            )
                        }
                    }.toTypedArray()
                ) { it.toList() }
            },
        setQueries.getByWorkoutId(workoutId = workoutId, limit = Long.MAX_VALUE)
            .asFlowList(dispatcher),
    ) { exercises, exerciseVariations, sets ->
        exercises.map { exercise ->
            Exercise(
                id = exercise.id,
                workoutId = workoutId,
                variationSets = exerciseVariations
                    .filter { it.second == exercise.id }
                    .map { (id, eId, variation) ->
                        VariationSets(
                            id = id,
                            variation = variation,
                            sets = sets.filter { it.variationId == variation.id }
                                .filter { it.date.toLocalDate() == it.date_ }
                                .map {
                                    LBSet(
                                        id = it.id,
                                        variationId = it.variationId,
                                        weight = it.weight ?: 0.0,
                                        reps = it.reps ?: 1,
                                        date = it.date,
                                        notes = it.notes ?: "",
                                        rpe = it.rpe?.toInt(),
                                        tempo = com.lift.bro.domain.models.Tempo(
                                            down = it.tempoDown ?: 3,
                                            hold = it.tempoHold ?: 1,
                                            up = it.tempoUp ?: 1,
                                        ),
                                        bodyWeightRep = variation.bodyWeight
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

    override suspend fun saveVariation(exerciseId: String, variationId: VariationId) {
        withContext(dispatcher) {
            exerciseQueries.saveVariation(
                id = uuid4().toString(),
                exerciseId = exerciseId,
                varationId = variationId,
            )
        }
    }

    override suspend fun delete(id: String) {
        withContext(dispatcher) {
            exerciseQueries.delete(id)
            exerciseQueries.deleteVariationsByExercise(id)
        }
    }

    override suspend fun deleteVariation(exerciseId: String, variationId: VariationId) {
        withContext(dispatcher) {
            exerciseQueries.deleteVariationBy(exerciseId, variationId)
        }
    }

    override suspend fun deleteVariationSets(variationSetId: String) {
        withContext(dispatcher) {
            exerciseQueries.deleteVariationsById(variationSetId)
        }
    }

    override suspend fun addExercise(workoutId: String, exerciseId: String) {
        withContext(dispatcher) {
            exerciseQueries.save(
                id = exerciseId,
                workoutId = workoutId,
            )
        }
    }
}
