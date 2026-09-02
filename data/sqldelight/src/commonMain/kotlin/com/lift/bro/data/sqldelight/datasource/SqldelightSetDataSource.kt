@file:OptIn(ExperimentalTime::class)

package com.lift.bro.data.sqldelight.datasource

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.ExerciseSectionId
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.MovementId
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import comliftbrodb.GetAllByMovement
import comliftbrodb.GetAllForLift
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
                sets.map { set ->
                    set.toDomain(setQueries.previousMaxFor(set.movementId, set.date))
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
                    set.toDomain(setQueries.previousMaxFor(set.movementId, set.date))
                }
            }

    override fun listen(id: String): Flow<LBSet?> =
        setQueries.get(id).asFlow().mapToOneOrNull(dispatcher).map { set ->
            set?.let {
                it.toDomain(setQueries.previousMaxFor(it.movementId, it.date))
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

private fun SetQueries.previousMaxFor(movementId: String, before: Instant?): Double? {
    val orm = getOneRepMaxForMovement(
        movementId = movementId,
        before = before
    ).executeAsOneOrNull()?.weight
    val emax = getEMaxForMovement(
        movementId = movementId,
        before = before
    ).executeAsOneOrNull()?.weight
    return orm ?: emax
}

fun GetAllSets.toDomain(previousMax: Double? = null): LBSet = toDomainSet(
    previousMax = previousMax,
    id = this.id,
    movementId = this.movementId,
    exerciseSectionId = this.exerciseSectionId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    failureRep = this.failureRep,
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
)

fun LiftingSet.toDomain(previousMax: Double? = null): LBSet = toDomainSet(
    previousMax = previousMax,
    id = this.id,
    movementId = this.movementId,
    exerciseSectionId = this.exerciseSectionId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    failureRep = this.failureRep,
    tempo = Tempo(
        down = this.tempoDown ?: 3,
        hold = this.tempoHold ?: 1,
        up = this.tempoUp ?: 1,
    ),
    date = this.date,
    notes = this.notes,
    rpe = this.rpe?.toInt(),
    videoUri = this.videoUri,
)

fun GetAllByMovement.toDomain(previousMax: Double? = null): LBSet = toDomainSet(
    previousMax = previousMax,
    id = this.id,
    movementId = this.movementId,
    exerciseSectionId = this.exerciseSectionId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    failureRep = this.failureRep,
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
)

fun GetAllForLift.toDomain(previousMax: Double? = null): LBSet = toDomainSet(
    previousMax = previousMax,
    id = this.id,
    movementId = this.movementId,
    exerciseSectionId = this.exerciseSectionId,
    weight = this.weight ?: 0.0,
    reps = this.reps ?: 1,
    failureRep = this.failureRep,
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
)

internal fun toDomainSet(
    previousMax: Double?,
    id: String,
    movementId: String,
    exerciseSectionId: ExerciseSectionId? = null,
    weight: Double = 0.0,
    reps: Long = 1,
    failureRep: Long? = null,
    tempo: Tempo = Tempo(),
    date: Instant,
    notes: String = "",
    rpe: Int? = null,
    bodyWeightRep: Boolean? = null,
    videoUri: String? = null,
): LBSet {
    val previousMaxWeight = previousMax?.takeIf { it > 0.0 }
    val merReps = failureRep?.minus(1)?.coerceIn(0, reps) ?: reps
    return LBSet(
        id = id,
        movementId = movementId,
        exerciseSectionId = exerciseSectionId,
        weight = weight,
        percentagePreviousMax = previousMaxWeight?.let { (weight / it).toFloat() },
        reps = reps,
        failureRep = failureRep,
        tempo = tempo,
        date = date,
        notes = notes,
        rpe = rpe,
        mer = previousMaxWeight?.let { calculateMer(weight, merReps, it) } ?: 0,
        bodyWeightRep = bodyWeightRep,
        videoUri = videoUri,
    )
}

internal fun calculateMer(setWeight: Double?, setReps: Long?, maxWeight: Double): Int {
    if (maxWeight <= 0.0) return 0
    val repFatigueCost = 4

    val weight = setWeight ?: 0.0
    val reps = setReps ?: 0

    val merFatigueThreshold = 80.0

    val setFatigue = ((weight / maxWeight) * 100.0) + (reps * repFatigueCost)

    return min(reps.toInt(), ((setFatigue - merFatigueThreshold) / 4.0).toInt())
}
