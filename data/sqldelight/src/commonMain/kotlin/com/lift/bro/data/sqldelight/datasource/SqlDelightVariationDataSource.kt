package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Category
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.CategoryQueries
import comliftbrodb.GetAllForCategory
import comliftbrodb.MovementQueries
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
class SqlDelightVariationDataSource(
    private val categoryQueries: CategoryQueries,
    private val setQueries: SetQueries,
    private val movementQueries: MovementQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
) : VariationDataSource {

    override suspend fun save(variation: Movement) {
        movementQueries.save(
            id = variation.id,
            categoryId = variation.lift?.id!!,
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
        combine(
            movementQueries.get(id).asFlow().mapToOneOrNull(dispatcher),
            setQueries.getAllByMovement(id, Long.MAX_VALUE).asFlow().mapToList(dispatcher),
        ) { variation, sets ->
            val lift = categoryQueries.get(variation?.categoryId ?: "").executeAsOneOrNull()
            variation?.toDomain(
                parentLift = lift?.toDomain(),
                sets = sets.map { it.toDomain() }
            )
        }.flowOn(dispatcher)

    override fun listenAll(): Flow<List<Movement>> =
        movementQueries.getAll().asFlow().mapToList(dispatcher).flatMapLatest { variations ->
            combine(
                *variations.map { variation ->
                    combine(
                        setQueries.getOneRepMaxForMovement(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                        setQueries.getEMaxForMovement(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                        setQueries.getMaxRepsForMovement(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                    ) { orm, volume, reps ->
                        Movement(
                            id = variation.id,
                            name = variation.name,
                            notes = variation.notes,
                            bodyWeight = variation.body_weight == 1L,
                            favourite = variation.favourite == 1L,
                            lift = Category(
                                id = variation.lift_id,
                                color = variation.lift_color?.toULong(),
                                name = variation.lift_name,
                            ),
                            oneRepMax = orm?.let { it.toDomain() },
                            eMax = volume?.let { it.toDomain() },
                            maxReps = reps?.let { it.toDomain() },
                        )
                    }
                }.toTypedArray()
            ) { it.toList() }
        }.flowOn(dispatcher)

    override fun listenAllForLift(liftId: String?): Flow<List<Movement>> =
        combine(
            movementQueries.getAllForCategory(liftId).asFlowList(),
            setQueries.getAllSets(
                limit = Long.MAX_VALUE,
                startDate = Instant.DISTANT_PAST,
                endDate = Instant.DISTANT_FUTURE,
                movementId = null,
                reps = null,
                sortBy = Sorting.date.toString(),
                order = 0,
            ).asFlowList(),
        ) { variations, sets ->
            variations.map { variation ->
                variation.toDomain(
                    parentLift = Category(
                        id = variation.id_,
                        color = variation.color?.toULong(),
                        name = variation.name_,
                    ),
                    sets = sets.filter { it.categoryId == variation.id }.map { it.toDomain() }
                )
            }
        }.flowOn(dispatcher)

    override fun get(id: String): Movement? {
        val variation = movementQueries.get(id).executeAsOneOrNull()
        val lift = categoryQueries.get(variation?.categoryId ?: "").executeAsOneOrNull()
        val sets = setQueries.getAllByMovement(id, Long.MAX_VALUE).executeAsList()
        return variation?.toDomain(
            parentLift = lift?.toDomain(),
            sets = sets.map { it.toDomain() }
        )
    }

    override fun getAll(): List<Movement> {
        val sets = setQueries.getAllSets(
            limit = Long.MAX_VALUE,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            movementId = null,
            sortBy = Sorting.date.toString(),
            order = 0,
            reps = null,
        ).executeAsList().map { it.toDomain() }
        return movementQueries.getAll().executeAsList().map { variation ->
            Movement(
                id = variation.id,
                name = variation.name,
                notes = variation.notes,
                favourite = variation.favourite == 1L,
                lift = Category(
                    id = variation.lift_id,
                    color = variation.lift_color?.toULong(),
                    name = variation.lift_name,
                ),
                oneRepMax = sets.filter { it.variationId == variation.id && it.reps == 1L }
                    .maxByOrNull { it.weight },
                eMax = sets.filter { it.variationId == variation.id && it.reps > 1 }
                    .maxByOrNull { it.weight * it.reps.toDouble() },
                maxReps = sets.filter { it.variationId == variation.id }
                    .maxByOrNull { it.reps },
                bodyWeight = variation.body_weight == 1L,
            )
        }
    }
}

private fun comliftbrodb.Lift.toDomain() = Category(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)
//
// private fun comliftbrodb.GetAll.toDomain(): Movement = Movement(
//    id = this.id,
//    lift = Category(
//        id = this.lift_id,
//        name = this.lift_name,
//        color = this.lift_color?.toULong(),
//    ),
//    name = this.name,
//    eMax = null,
//    maxReps = null,
//    oneRepMax = null,
//    bodyWeight = this.body_weight?.let { it == 1L },
// )

private fun comliftbrodb.Movement.toDomain(
    parentLift: Category?,
    sets: List<LBSet>,
): Movement = Movement(
    id = this.id,
    lift = parentLift,
    name = this.name,
    eMax = sets.filter { it.reps > 1 }.maxByOrNull { it.reps * it.weight },
    maxReps = sets.maxByOrNull { it.reps },
    oneRepMax = sets.filter { it.reps == 1L }.maxByOrNull { it.weight },
    latestSet = sets.maxByOrNull { it.date },
    favourite = this.favourite == 1L,
    notes = this.notes,
    bodyWeight = this.body_weight?.let { it == 1L },
)

private fun GetAllForCategory.toDomain(
    parentLift: Category?,
    sets: List<LBSet>,
): Movement = Movement(
    id = this.id,
    lift = parentLift,
    name = this.name,
    eMax = sets.filter { (it.reps ?: 1) > 1 }.maxByOrNull { (it.reps ?: 1) * it.weight },
    maxReps = sets.maxByOrNull { it.reps ?: 1 },
    oneRepMax = sets.filter { (it.reps ?: 1) == 1L }.maxByOrNull { it.weight },
    favourite = this.favourite == 1L,
    notes = this.notes,
    bodyWeight = this.body_weight?.let { it == 1L },
)

private fun comliftbrodb.GetAllByMovement.toDomain(): LBSet = LBSet(
    id = this.id,
    variationId = this.movementId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    tempo = com.lift.bro.domain.models.Tempo(
        down = this.tempoDown ?: 3,
        hold = this.tempoHold ?: 1,
        up = this.tempoUp ?: 1,
    ),
    date = this.date,
    notes = this.notes ?: "",
    rpe = this.rpe?.toInt(),
    bodyWeightRep = this.body_weight?.let { it == 1L },
)
