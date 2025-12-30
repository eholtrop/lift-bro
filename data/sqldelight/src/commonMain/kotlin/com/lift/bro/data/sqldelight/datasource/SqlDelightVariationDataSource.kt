package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.LiftQueries
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import comliftbrodb.variation.GetAllForLift
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.Instant

class SqlDelightVariationDataSource(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): VariationDataSource {

    override suspend fun save(variation: Variation) {
        variationQueries.save(
            id = variation.id,
            liftId = variation.lift?.id!!,
            name = variation.name,
            notes = variation.notes,
            favourite = if (variation.favourite) 1 else 0,
            body_weight = variation.bodyWeight?.let { if (it) 1 else 0 }
        )
    }

    override suspend fun delete(id: String) {
        variationQueries.delete(id)
    }

    override suspend fun deleteAll() {
        variationQueries.deleteAll()
    }

    override fun listen(id: String): Flow<Variation?> =
        combine(
            variationQueries.get(id).asFlow().mapToOneOrNull(dispatcher),
            setQueries.getAllByVariation(id, Long.MAX_VALUE).asFlow().mapToList(dispatcher),
        ) { variation, sets ->
            val lift = liftQueries.get(variation?.liftId ?: "").executeAsOneOrNull()
            variation?.toDomain(
                parentLift = lift?.toDomain(),
                sets = sets.map { it.toDomain() }
            )
        }.flowOn(dispatcher)

    override fun listenAll(): Flow<List<Variation>> =
        variationQueries.getAll().asFlow().mapToList(dispatcher).flatMapLatest { variations ->
            combine(
                *variations.map { variation ->
                    combine(
                        setQueries.getOneRepMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                        setQueries.getEMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                        setQueries.getMaxRepsForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(dispatcher),
                    ) { orm, volume, reps ->
                        Variation(
                            id = variation.id,
                            name = variation.name,
                            notes = variation.notes,
                            bodyWeight = variation.body_weight == 1L,
                            favourite = variation.favourite == 1L,
                            lift = Lift(
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

    override fun listenAllForLift(liftId: String?): Flow<List<Variation>> =
        combine(
            variationQueries.getAllForLift(liftId).asFlowList(),

            setQueries.getAll(
                limit = Long.MAX_VALUE,
                startDate = Instant.DISTANT_PAST,
                endDate = Instant.DISTANT_FUTURE,
                variationId = null,
                sortBy = Sorting.date.toString(),
                order = 0,
            ).asFlowList(),
        ) { variations: List<GetAllForLift>, sets: List<LiftingSet> ->
            variations.map { variation ->
                variation.toDomain(
                    parentLift = Lift(
                        id = variation.id_,
                        color = variation.color?.toULong(),
                        name = variation.name_,
                    ),
                    sets = sets.filter { it.variationId == variation.id }.map { it.toDomain() }
                )
            }
        }.flowOn(dispatcher)

    override fun get(id: String): Variation? {
        val variation = variationQueries.get(id).executeAsOneOrNull()
        val lift = liftQueries.get(variation?.liftId ?: "").executeAsOneOrNull()
        val sets = setQueries.getAllByVariation(id, Long.MAX_VALUE).executeAsList()
        return variation?.toDomain(
            parentLift = lift?.toDomain(),
            sets = sets.map { it.toDomain() }
        )
    }

    override fun getAll(): List<Variation> {
        val sets = setQueries.getAll(
            limit = Long.MAX_VALUE,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            variationId = null,
            sortBy = Sorting.date.toString(),
            order = 0,
        ).executeAsList().map { it.toDomain() }
        return variationQueries.getAll().executeAsList().map { variation ->
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

private fun comliftbrodb.Lift.toDomain() = Lift(
    id = this.id,
    name = this.name,
    color = this.color?.toULong(),
)

private fun comliftbrodb.GetAll.toDomain(): Variation = Variation(
    id = this.id,
    lift = Lift(
        id = this.lift_id,
        name = this.lift_name,
        color = this.lift_color?.toULong(),
    ),
    name = this.name,
    eMax = null,
    maxReps = null,
    oneRepMax = null,
    bodyWeight = this.body_weight?.let { it == 1L },
)

private fun comliftbrodb.Variation.toDomain(
    parentLift: Lift?,
    sets: List<LBSet>,
): Variation = Variation(
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


private fun GetAllForLift.toDomain(
    parentLift: Lift?,
    sets: List<LBSet>,
): Variation = Variation(
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

private fun comliftbrodb.GetAllByVariation.toDomain(): LBSet = LBSet(
    id = this.id,
    variationId = this.variationId,
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
