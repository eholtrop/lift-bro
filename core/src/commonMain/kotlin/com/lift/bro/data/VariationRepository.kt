package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.data.datasource.flowToOneOrNull
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.repositories.IVariationRepository
import comliftbrodb.LiftQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant

class VariationRepository(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO,
): IVariationRepository {

    override suspend fun deleteAll() {
        variationQueries.deleteAll()
    }

    override fun save(variation: Variation) {
        GlobalScope.launch {
            variationQueries.save(
                id = variation.id,
                liftId = variation.lift?.id!!,
                name = variation.name,
                notes = variation.notes,
                favourite = if (variation.favourite) 1 else 0,
                body_weight = variation.bodyWeight?.let { if (it) 1 else 0 }
            )
        }
    }

    // only used for backup, will not populate emax/max
    override fun getAll(): List<Variation> {
        val parentLift = liftQueries.getAll().executeAsList().map { it.toDomain() }
        val sets = setQueries.getAll(
            limit = Long.MAX_VALUE,
            startDate = Instant.DISTANT_PAST,
            endDate = Instant.DISTANT_FUTURE,
            variationId = null
        ).executeAsList()
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
            )
        }
    }

    override fun listenAll(liftId: String): Flow<List<Variation>> {
        return combine(
            liftQueries.get(liftId).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() },
            variationQueries.getAllForLift(liftId).asFlow().mapToList(Dispatchers.IO),
            setQueries.getAll(
                limit = Long.MAX_VALUE,
                startDate = Instant.DISTANT_PAST,
                endDate = Instant.DISTANT_FUTURE,
                variationId = null
            ).asFlow().mapToList(Dispatchers.IO)
        ) { lift, variations, sets ->
            variations.map { variation ->
                variation.toDomain(
                    parentLift = lift,
                    sets = sets.filter { it.variationId == variation.id }
                )
            }
        }
    }

    override fun listenAll(): Flow<List<Variation>> =
        variationQueries.getAll().flowToList(dispatcher).flatMapLatest { variations ->
            combine(
                *variations.map { variation ->
                    combine(
                        setQueries.getOneRepMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .flowToOneOrNull(),
                        setQueries.getEMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .flowToOneOrNull(),
                        setQueries.getMaxRepsForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .flowToOneOrNull(),
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
        }

    override fun delete(id: String) {
        GlobalScope.launch {
            variationQueries.delete(id)
        }
    }

    override fun listen(id: String): Flow<Variation?> {
        return combine(
            variationQueries.get(id).asFlow().mapToOneOrNull(Dispatchers.IO),
            setQueries.getAllByVariation(id, Long.MAX_VALUE).asFlow().mapToList(Dispatchers.IO),
        ) { variation, sets ->
            val lift = liftQueries.get(variation?.liftId ?: "").executeAsOneOrNull()

            variation?.toDomain(
                lift?.toDomain(),
                sets
            )
        }
    }

    override fun get(variationId: String?): Variation? {
        val variation = variationQueries.get(variationId ?: "").executeAsOneOrNull()
        val lift = liftQueries.get(variation?.liftId ?: "").executeAsOneOrNull()
        val sets = setQueries.getAllByVariation(variationId ?: "", Long.MAX_VALUE).executeAsList()

        return variation?.toDomain(
            lift?.toDomain(),
            sets,
        )
    }
}

fun buildLBSet(
    ormId: String?,
    ormDate: Instant?,
    ormNotes: String?,
    ormReps: Long?,
    ormRpe: Long?,
    ormTempoDown: Long?,
    ormTempoUp: Long?,
    ormTempoHold: Long?,
    ormWeight: Double?,
    variationId: VariationId,
) = let(
    ormId,
    ormDate,
    ormNotes,
    ormReps,
    ormTempoDown,
    ormTempoUp,
    ormTempoHold,
    ormWeight,
) { id, date, notes, reps, tempoDown, tempoUp, tempoHold, weight ->
    LBSet(
        id = id,
        variationId = variationId,
        weight = weight,
        reps = reps,
        date = date,
        notes = notes,
        rpe = ormRpe?.toInt(),
        tempo = com.lift.bro.domain.models.Tempo(
            down = tempoDown,
            hold = tempoHold,
            up = tempoUp,
        )
    )
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, R> let(
    i1: T1?,
    i2: T2?,
    i3: T3?,
    i4: T4?,
    i5: T5?,
    i6: T6?,
    i7: T7?,
    i8: T8?,
    block: (T1, T2, T3, T4, T5, T6, T7, T8) -> R,
): R? {
    if (i1 == null || i2 == null || i3 == null || i4 == null || i5 == null || i6 == null || i7 == null || i8 == null) return null
    return block(i1, i2, i3, i4, i5, i6, i7, i8)
}

fun <T1, T2, T3, T4, T5, T6, T7, T8, T9, R> let(
    i1: T1?,
    i2: T2?,
    i3: T3?,
    i4: T4?,
    i5: T5?,
    i6: T6?,
    i7: T7?,
    i8: T8?,
    i9: T9?,
    block: (T1, T2, T3, T4, T5, T6, T7, T8, T9) -> R,
): R? {
    if (i1 == null || i2 == null || i3 == null || i4 == null || i5 == null || i6 == null || i7 == null || i8 == null || i9 == null) return null
    return block(i1, i2, i3, i4, i5, i6, i7, i8, i9)
}

fun <T, R> let(
    vararg items: T?,
    block: (List<T>) -> R,
): R? {
    if (items.any { it == null }) return null
    return block(items.filterNotNull())
}