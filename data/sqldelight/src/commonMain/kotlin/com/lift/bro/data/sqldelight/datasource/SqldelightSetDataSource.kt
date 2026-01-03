package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlin.collections.map
import kotlin.math.min

class SqldelightSetDataSource(
    private val setQueries: SetQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): SetDataSource {

    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
        reps: Long?,
        sorting: Sorting,
        order: Order,
    ): Flow<List<LBSet>> {
        return setQueries.getAll(
            startDate = startDate?.atStartOfDayIn(),
            endDate = endDate?.atEndOfDayIn(),
            variationId = variationId,
            limit = limit,
            sortBy = sorting.toString(),
            order = if (order == Order.Descending) 0 else 1,
            reps = reps,
        )
            .asFlow().mapToList(dispatcher)
            .map { sets ->
                sets
                    .map { set ->
                        val orm = setQueries.getOneRepMaxForVariation(
                            variationId = set.variationId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight
                        val emax = setQueries.getEMaxForVariation(
                            variationId = set.variationId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight

                        set.toDomain().copy(
                            mer = (orm ?: emax)?.let { calculateMer(set.weight, set.reps, it) } ?: 0
                        )
                    }
            }
    }

    override fun listenAllForLift(liftId: String, limit: Long, sorting: Sorting): Flow<List<LBSet>> =
        setQueries.getAllForLift(
            liftId = liftId,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            limit = limit,
            sortBy = sorting.toString()
        )
            .asFlow().mapToList(dispatcher)
            .map {
                it.map {
                    LBSet(
                        id = it.id,
                        variationId = it.variationId,
                        weight = it.weight ?: 0.0,
                        reps = it.reps ?: 1,
                        tempo = Tempo(
                            down = it.tempoDown ?: 3,
                            hold = it.tempoHold ?: 1,
                            up = it.tempoUp ?: 1,
                        ),
                        date = it.date,
                        notes = it.notes,
                        rpe = it.rpe?.toInt(),
                        mer = 0,
                        bodyWeightRep = it.body_weight?.let { it == 1L },
                    )
                }
            }

    override fun listen(id: String): Flow<LBSet?> =
        setQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { it?.toDomain() }

    override suspend fun save(lbSet: LBSet) {
        setQueries.save(
            id = lbSet.id,
            variationId = lbSet.variationId,
            weight = lbSet.weight,
            reps = lbSet.reps,
            tempoDown = lbSet.tempo.down,
            tempoHold = lbSet.tempo.hold,
            tempoUp = lbSet.tempo.up,
            date = lbSet.date,
            notes = lbSet.notes,
            rpe = lbSet.rpe?.toLong(),
        )
    }

    override suspend fun delete(lbSet: LBSet) {
        setQueries.delete(lbSet.id)
    }

    override suspend fun deleteAll() {
        setQueries.deleteAll()
    }

    override suspend fun deleteAll(variationId: VariationId) {
        setQueries.deleteAllFromVariations(variationId)
    }
}

fun LiftingSet.toDomain() = LBSet(
    id = this.id,
    variationId = this.variationId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    tempo = Tempo(
        down = this.tempoDown ?: 3,
        hold = this.tempoHold ?: 1,
        up = this.tempoUp ?: 1,
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe?.toInt(),
)

private fun calculateMer(setWeight: Double?, setReps: Long?, maxWeight: Double): Int {
    if (maxWeight <= 0.0) return 0
    val repFatigueCost = 4

    val weight = setWeight ?: 0.0
    val reps = setReps ?: 0

    val merFatigueThreshold = 80.0

    val setFatigue = ((weight / maxWeight) * 100.0) + (reps * repFatigueCost)

    return min(reps.toInt(), ((setFatigue - merFatigueThreshold) / 4.0).toInt())
}

private fun LocalDate.atStartOfDayIn(): Instant = this.atStartOfDayIn(TimeZone.currentSystemDefault())

private fun LocalDate.atEndOfDayIn(): Instant =
    this.atTime(23, 59, 59, 999).toInstant(TimeZone.currentSystemDefault())
