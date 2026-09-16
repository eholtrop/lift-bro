@file:OptIn(ExperimentalTime::class)

package com.lift.bro.data.sqldelight.datasource

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.ifLet
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.RecommendedSet
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.SetTarget
import com.lift.bro.domain.models.Tempo
import comliftbrodb.ExerciseQueries
import comliftbrodb.GetAll
import comliftbrodb.GetByWorkoutId
import comliftbrodb.GetExerciseSectionsByWorkoutId
import comliftbrodb.MovementQueries
import comliftbrodb.RecommendedSetQueries
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.collections.emptyList
import kotlin.time.ExperimentalTime

class SqldelightExerciseDataSource(
    private val exerciseQueries: ExerciseQueries,
    private val setQueries: SetQueries,
    private val recommendedSetQueries: RecommendedSetQueries,
    private val movementQueries: MovementQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ExerciseDataSource {

    override fun listenAll(workoutId: String?): Flow<List<Exercise>> = combine(
        exerciseQueries.getByWorkoutId(workoutId ?: "").asFlowList(dispatcher),
        setQueries.getByWorkoutId(workoutId = workoutId ?: "", limit = Long.MAX_VALUE).asFlowList(dispatcher),
        movementQueries.getAll().asFlowList(),
        exerciseQueries.getExerciseSectionsByWorkoutId(workoutId ?: "").asFlowList(),
        recommendedSetQueries.getRecommendedSetByWorkoutId(
            workoutId ?: "",
            limit = Long.MAX_VALUE
        ).asFlowList()
    ) { exercises, sets, movements, sections, rSets ->
        exercises.map { exercise ->
            Exercise(
                id = exercise.id,
                workoutId = workoutId ?: "",
                sections = sections.filter { it.exercise_id == exercise.id }.map { section ->
                    section.toDomainSection(
                        sets = sets,
                        movements = movements,
                        sections = sections,
                        recommendedSets = rSets.map { rSet ->
                            RecommendedSet(
                                id = rSet.id,
                                target = when (rSet.type) {
                                    "percentageMax" -> ifLet(rSet.percentage, rSet.reps) { percentage, reps ->
                                        SetTarget.PercentageMax(
                                            percentage = percentage.toFloat(),
                                            reps = reps,
                                        )
                                    } ?: SetTarget.Unsupported

                                    "weight" -> SetTarget.Weight(
                                        weight = rSet.weight ?: 0.0,
                                        reps = rSet.reps ?: 1,
                                    )

                                    "reps" -> SetTarget.Reps(
                                        reps = rSet.reps ?: 1,
                                        addedWeight = rSet.weight ?: 0.0,
                                    )

                                    else -> SetTarget.Unsupported
                                },
                                tempo = Tempo(),
                                movement = movements.first { it.id == rSet.movementId }.toDomain(),
                                notes = rSet.notes,
                                sectionId = rSet.exerciseSectionId,
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
        }
    }

    override suspend fun delete(id: ExerciseId) {
        exerciseQueries.delete(id)
        exerciseQueries.deleteSectionsByExercise(exerciseId = id)
        setQueries.deleteAllForExercise(exerciseId = id)
    }

    override suspend fun save(section: Section) {
        exerciseQueries.saveSection(
            id = section.id,
            exerciseId = section.exerciseId,
            name = null,
            sort_order = 0L,
            primaryMovementId = section.primaryMovement?.id,
            referenceSectionId = section.referenceSection?.id,
        )
        section.recommendedSets.forEach { rs ->
            when (val target = rs.target) {
                is SetTarget.PercentageMax -> recommendedSetQueries.save(
                    id = rs.id,
                    movementId = rs.movement.id,
                    type = "percentageMax",
                    percentage = target.percentage.toDouble(),
                    reps = target.reps,
                    exerciseSectionId = rs.sectionId,
                    weight = null,
                    tempoDown = rs.tempo.down,
                    tempoHold = rs.tempo.hold,
                    tempoUp = rs.tempo.up,
                    notes = rs.notes,
                )

                is SetTarget.Reps -> recommendedSetQueries.save(
                    id = rs.id,
                    movementId = rs.movement.id,
                    type = "reps",
                    percentage = null,
                    weight = target.addedWeight,
                    reps = target.reps,
                    tempoDown = rs.tempo.down,
                    tempoHold = rs.tempo.hold,
                    tempoUp = rs.tempo.up,
                    notes = rs.notes,
                    exerciseSectionId = rs.sectionId,
                )

                SetTarget.Unsupported -> {
                }

                is SetTarget.Weight -> recommendedSetQueries.save(
                    id = rs.id,
                    movementId = rs.movement.id,
                    type = "weight",
                    percentage = null,
                    weight = target.weight,
                    reps = target.reps,
                    tempoDown = rs.tempo.down,
                    tempoHold = rs.tempo.hold,
                    tempoUp = rs.tempo.up,
                    notes = rs.notes,
                    exerciseSectionId = rs.sectionId,
                )
            }
        }
    }

    override suspend fun delete(section: Section, cascading: Boolean) {
        exerciseQueries.deleteSectionsById(section.id)
        if (cascading) {
            setQueries.deleteAllForSections(section.id)
        }
        recommendedSetQueries.deleteAllForSections(section.id)
    }

    override suspend fun deleteRecommendedSet(recommendedSetId: String) {
        recommendedSetQueries.delete(recommendedSetId)
    }

    override suspend fun deleteAll() {
        withContext(dispatcher) {
            exerciseQueries.deleteAll()
        }
    }
}

private fun GetExerciseSectionsByWorkoutId.toDomainSection(
    sets: List<GetByWorkoutId>,
    recommendedSets: List<RecommendedSet>,
    movements: List<GetAll>,
    sections: List<GetExerciseSectionsByWorkoutId>,
): Section = Section(
    id = this.exercise_section_id,
    exerciseId = this.exercise_id,
    movements = movements.filter { movement ->
        sets.filter {
            it.exerciseSectionId == this.exercise_section_id
        }.any { it.movementId == movement.id }
    }.map { it.toDomain() },
    referenceSection = sections.firstOrNull { it.exercise_section_id == this.reference_section_id }?.toDomainSection(
        sets = sets,
        movements = movements,
        sections = emptyList(), // avoid infinite loop 😭
        recommendedSets = emptyList(),
    ),
    primaryMovement = movements.firstOrNull { it.id == this.primary_movement_id }?.toDomain(),
    sets = sets.filter { it.exerciseSectionId == this.exercise_section_id }.map {
        LBSet(
            id = it.id,
            movementId = it.movementId,
            exerciseSectionId = it.exerciseSectionId,
            weight = it.weight ?: 0.0,
            reps = it.reps ?: 1,
            date = it.date,
            notes = it.notes,
            rpe = it.rpe?.toInt(),
            tempo = Tempo(
                down = it.tempoDown ?: 3,
                up = it.tempoUp ?: 1,
                hold = it.tempoHold ?: 1,
            ),
            bodyWeightRep = it.body_weight?.let { it == 1L },
            failureRep = it.failureRep,
        )
    },
    recommendedSets = recommendedSets.filter {
        it.sectionId == this.exercise_section_id
    },
)
