@file:OptIn(ExperimentalTime::class)

package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.MovementId
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.GetAllByMovement
import comliftbrodb.GetAllSets
import comliftbrodb.LiftingSet
import comliftbrodb.SetQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import tv.dpal.ktx.datetime.atEndOfDayIn
import tv.dpal.ktx.datetime.atStartOfDayIn
import tv.dpal.logging.Log
import tv.dpal.logging.d
import kotlin.math.min
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

private fun successfulReps(reps: Long?, failureRep: Long?) = failureRep ?: reps

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
        return setQueries.getAllSets(
            startDate = startDate?.atStartOfDayIn(),
            endDate = endDate?.atEndOfDayIn(),
            movementId = variationId,
            limit = limit,
            sortBy = sorting.toString(),
            order = if (order == Order.Descending) 0 else 1,
            reps = reps,
        )
            .asFlow().mapToList(dispatcher)
            .map { sets ->
                sets
                    .map { set ->
                        val orm = setQueries.getOneRepMaxForMovement(
                            movementId = set.movementId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight
                        val emax = setQueries.getEMaxForMovement(
                            movementId = set.movementId,
                            before = set.date
                        ).executeAsOneOrNull()?.weight

                        set.toDomain().copy(
                            percentagePreviousMax = (orm ?: emax)?.let { set.weight?.div(it) }?.toFloat(),
                            mer = (orm ?: emax)?.let { calculateMer(set.weight, successfulReps(set.reps, set.failureRep), it) } ?: 0,
                        )
                    }
            }
    }

    override fun listenAllForLift(liftId: String?, limit: Long, sorting: Sorting): Flow<List<LBSet>> =
        setQueries.getAllForLift(
            categoryId = liftId,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            limit = limit,
            sortBy = sorting.toString()
        )
            .asFlow().mapToList(dispatcher)
            .map { sets ->
                sets.map { set ->
                    val orm = setQueries.getOneRepMaxForMovement(
                        movementId = set.movementId,
                        before = set.date
                    ).executeAsOneOrNull()?.weight
                    val emax = setQueries.getEMaxForMovement(
                        movementId = set.movementId,
                        before = set.date
                    ).executeAsOneOrNull()?.weight
                    LBSet(
                        id = set.id,
                        movementId = set.movementId,
                        weight = set.weight ?: 0.0,
                        reps = set.reps ?: 1,
                        tempo = Tempo(
                            down = set.tempoDown ?: 3,
                            hold = set.tempoHold ?: 1,
                            up = set.tempoUp ?: 1,
                        ),
                        percentagePreviousMax = (orm ?: emax)?.let { set.weight?.div(it) }?.toFloat(),
                        mer = (orm ?: emax)?.let { calculateMer(set.weight, successfulReps(set.reps, set.failureRep), it) } ?: 0,
                        date = set.date,
                        notes = set.notes,
                        rpe = set.rpe?.toInt(),
                        bodyWeightRep = set.body_weight?.let { it == 1L },
                        videoUri = set.videoUri,
                        failureRep = set.failureRep,
                    )
                }
            }

    override fun listen(id: String): Flow<LBSet?> =
        setQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { set ->
            set?.let {
                val orm = setQueries.getOneRepMaxForMovement(
                    movementId = set.movementId,
                    before = set.date
                ).executeAsOneOrNull()?.weight
                val emax = setQueries.getEMaxForMovement(
                    movementId = set.movementId,
                    before = set.date
                ).executeAsOneOrNull()?.weight

                set.toDomain().copy(
                    percentagePreviousMax = (orm ?: emax)?.let { set.weight?.div(it) }?.toFloat(),
                    mer = (orm ?: emax)?.let { calculateMer(set.weight, successfulReps(set.reps, set.failureRep), it) } ?: 0,
                )
            }
        }


    override suspend fun save(lbSet: LBSet) {
        Log.d("LiftBroDb", "saving $lbSet")
        setQueries.save(
            id = lbSet.id,
            movementId = lbSet.movementId,
            exerciseSectionId = lbSet.exerciseSectionId,
            weight = lbSet.weight,
            reps = lbSet.reps,
            tempoDown = lbSet.tempo.down,
            tempoHold = lbSet.tempo.hold,
            tempoUp = lbSet.tempo.up,
            date = lbSet.date,
            notes = lbSet.notes,
            rpe = lbSet.rpe?.toLong(),
            videoUri = lbSet.videoUri,
            failureRep = lbSet.failureRep,
        )
    }

    override suspend fun delete(lbSet: LBSet) {
        setQueries.delete(lbSet.id)
    }

    override suspend fun deleteAll() {
        setQueries.deleteAll()
    }

    override suspend fun deleteAll(variationId: MovementId) {
        setQueries.deleteAllFromVariations(variationId)
    }
}

fun GetAllSets.toDomain() = LBSet(
    id = this.id,
    movementId = this.movementId,
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
    bodyWeightRep = this.body_weight?.let { it == 1L },
    videoUri = this.videoUri,
    exerciseSectionId = this.exerciseSectionId,
    failureRep = this.failureRep,
)

fun LiftingSet.toDomain() = LBSet(
    id = this.id,
    movementId = this.movementId,
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
    videoUri = this.videoUri,
    exerciseSectionId = this.exerciseSectionId,
    failureRep = this.failureRep,
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
