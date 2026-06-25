package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.models.Tempo
import comliftbrodb.GetAll
import comliftbrodb.GetAllForCategory
import comliftbrodb.GetWithKeySets
import comliftbrodb.MovementQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SqlDelightVariationDataSource(
    private val movementQueries: MovementQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VariationDataSource {

    override suspend fun save(variation: Movement) {
        movementQueries.save(
            id = variation.id,
            categoryId = variation.lift?.id,
            name = variation.name,
            notes = variation.notes,
            favourite = if (variation.favourite) 1 else 0,
            body_weight = variation.bodyWeight?.let { if (it) 1 else 0 }
        )
    }

    override suspend fun delete(id: String) {
        movementQueries.delete(id)
    }

    override suspend fun deleteAll() {
        movementQueries.deleteAll()
    }

    override fun listen(id: String): Flow<Movement?> =
        movementQueries.getWithKeySets(id).asFlow().mapToOneOrNull(dispatcher)
            .map { it?.toDomain() }
            .flowOn(dispatcher)

    override fun listenAll(): Flow<List<Movement>> =
        movementQueries.getAll().asFlow().mapToList(dispatcher)
            .map { it.map { row -> row.toDomain() } }
            .flowOn(dispatcher)

    override fun listenAllForLift(liftId: String?): Flow<List<Movement>> =
        movementQueries.getAllForCategory(liftId).asFlowList()
            .map { it.map { row -> row.toDomain() } }
            .flowOn(dispatcher)

    override fun get(id: String): Movement? =
        movementQueries.getWithKeySets(id).executeAsOneOrNull()?.toDomain()

    override fun getAll(): List<Movement> =
        movementQueries.getAll().executeAsList().map { it.toDomain() }
}
fun GetAll.toDomain(): Movement = Movement(
    id = id,
    lift = Category(
        id = category_id,
        color = category_color?.toULong(),
        name = category_name,
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

private fun GetAllForCategory.toDomain(): Movement = Movement(
    id = id,
    lift = Category(
        id = category_id,
        color = category_color?.toULong(),
        name = category_name,
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

private fun GetWithKeySets.toDomain(): Movement = Movement(
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
