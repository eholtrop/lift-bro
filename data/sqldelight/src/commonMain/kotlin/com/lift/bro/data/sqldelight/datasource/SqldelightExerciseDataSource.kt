@file:OptIn(ExperimentalTime::class)

package com.lift.bro.data.sqldelight.datasource

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.ExerciseId
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Section
import com.lift.bro.domain.models.Tempo
import comliftbrodb.ExerciseQueries
import comliftbrodb.GetAll
import comliftbrodb.GetAllForWorkout
import comliftbrodb.GetByWorkoutId
import comliftbrodb.GetExerciseSectionsByWorkoutId
import comliftbrodb.MovementQueries
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class SqldelightExerciseDataSource(
    private val exerciseQueries: ExerciseQueries,
    private val setQueries: SetQueries,
    private val movementQueries: MovementQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): ExerciseDataSource {

    override fun listenAll(workoutId: String?): Flow<List<Exercise>> = combine(
        exerciseQueries.getByWorkoutId(workoutId ?: "").asFlowList(dispatcher),
        setQueries.getByWorkoutId(workoutId = workoutId ?: "", limit = Long.MAX_VALUE)
            .asFlowList(dispatcher),
        movementQueries.getAll().asFlowList(),
        exerciseQueries.getExerciseSectionsByWorkoutId(workoutId ?: "").asFlowList()
    ) { exercises, sets, movements, sections ->
        exercises.map { exercise ->
            Exercise(
                id = exercise.id,
                workoutId = workoutId ?: "",
                sections = sections
                    .filter { it.exercise_id == exercise.id }
                    .map { section ->
                        section.toDomain(
                            sets = sets,
                            movements = movements,
                            sections = sections,
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
    }

    override suspend fun delete(section: Section, cascading: Boolean) {
        exerciseQueries.deleteSectionsById(section.id)
        if (cascading) {
            setQueries.deleteAllForSections(section.id)
        }
    }
}

private fun GetExerciseSectionsByWorkoutId.toDomain(
    sets: List<GetByWorkoutId>,
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
    referenceSection = sections.firstOrNull { it.exercise_section_id == this.reference_section_id }?.toDomain(
        sets = sets,
        movements = movements,
        sections = emptyList(), // avoid infinite loop 😭
    ),
    primaryMovement = movements.firstOrNull { it.id == this.primary_movement_id }?.toDomain(),
    sets = sets.filter { it.exerciseSectionId == this.exercise_section_id }
        .map {
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
                bodyWeightRep = it.body_weight?.let { it == 1L }
            )
        }
)

private fun GetAllForWorkout.toDomain(): Movement = Movement(
    id = id,
    lift = Category(
        id = lift_id,
        color = lift_color?.toULong(),
        name = lift_name,
    ),
    name = name,
    notes = notes,
    favourite = favourite == 1L,
    bodyWeight = body_weight?.let { it == 1L },
    oneRepMax = asLBSet(
        id = orm_id,
        movementId = orm_movementId,
        weight = orm_weight,
        reps = orm_reps,
        tempoDown = orm_tempoDown,
        tempoHold = orm_tempoHold,
        tempoUp = orm_tempoUp,
        date = orm_date,
        notes = orm_notes,
        rpe = orm_rpe,
        videoUri = orm_videoUri,
        exerciseSectionId = orm_exerciseSectionId,
    ),
    latestSet = asLBSet(
        id = latest_id,
        movementId = latest_movementId,
        weight = latest_weight,
        reps = latest_reps,
        tempoDown = latest_tempoDown,
        tempoHold = latest_tempoHold,
        tempoUp = latest_tempoUp,
        date = latest_date,
        notes = latest_notes,
        rpe = latest_rpe,
        videoUri = latest_videoUri,
        exerciseSectionId = latest_exerciseSectionId,
    ),
    eMax = asLBSet(
        id = emax_id,
        movementId = emax_movementId,
        weight = emax_weight,
        reps = emax_reps,
        tempoDown = emax_tempoDown,
        tempoHold = emax_tempoHold,
        tempoUp = emax_tempoUp,
        date = emax_date,
        notes = emax_notes,
        rpe = emax_rpe,
        videoUri = emax_videoUri,
        exerciseSectionId = emax_exerciseSectionId,
    ),
    maxReps = asLBSet(
        id = maxreps_id,
        movementId = maxreps_movementId,
        weight = maxreps_weight,
        reps = maxreps_reps,
        tempoDown = maxreps_tempoDown,
        tempoHold = maxreps_tempoHold,
        tempoUp = maxreps_tempoUp,
        date = maxreps_date,
        notes = maxreps_notes,
        rpe = maxreps_rpe,
        videoUri = maxreps_videoUri,
        exerciseSectionId = maxreps_exerciseSectionId,
    ),
)

@Suppress("LongParameterList")
private fun asLBSet(
    id: String?,
    movementId: String?,
    weight: Double?,
    reps: Long?,
    tempoDown: Long?,
    tempoHold: Long?,
    tempoUp: Long?,
    date: kotlin.time.Instant?,
    notes: String?,
    rpe: Long?,
    videoUri: String?,
    exerciseSectionId: String?,
): LBSet? = id?.let {
    LBSet(
        id = it,
        movementId = movementId ?: "",
        weight = weight ?: 0.0,
        reps = reps ?: 1,
        tempo = Tempo(
            down = tempoDown ?: 3,
            hold = tempoHold ?: 1,
            up = tempoUp ?: 1,
        ),
        date = date ?: Clock.System.now(),
        notes = notes ?: "",
        rpe = rpe?.toInt(),
        videoUri = videoUri,
        exerciseSectionId = exerciseSectionId,
    )
}
