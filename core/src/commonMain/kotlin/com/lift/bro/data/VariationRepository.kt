package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.debug
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import com.lift.bro.utils.mapEach
import comliftbrodb.LiftQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMap
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.atStartOfDayIn
import kotlin.time.Clock.System.now

class VariationRepository(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
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
                favourite = if (variation.favourite) 1 else 0
            )
        }
    }

    // only used for backup, will not populate emax/max
    override fun getAll(): List<Variation> {
        val parentLift = liftQueries.getAll().executeAsList().map { it.toDomain() }
        val sets = setQueries.getAll(limit = Long.MAX_VALUE, startDate = Instant.DISTANT_PAST, endDate = Instant.DISTANT_FUTURE).executeAsList()
        return variationQueries.getAll().executeAsList().map { variation ->
            variation.toDomain(
                parentLift.firstOrNull { it.id == variation.liftId },
                sets.filter { it.variationId == variation.id }
            )
        }
    }

    override fun listenAll(liftId: String): Flow<List<Variation>> {
        return combine(
            liftQueries.get(liftId).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() },
            variationQueries.getAllForLift(liftId).asFlow().mapToList(Dispatchers.IO),
            setQueries.getAll(limit = Long.MAX_VALUE, startDate = Instant.DISTANT_PAST, endDate = Instant.DISTANT_FUTURE).asFlow().mapToList(Dispatchers.IO)
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
        variationQueries.getAll().asFlow().mapToList(Dispatchers.IO)
            .flatMapLatest { variations ->
                val flows = variations.map { variation ->
                    combine(
                        liftQueries.get(variation.liftId).asFlow().mapToOneOrNull(Dispatchers.IO),
                        setQueries.getOneRepMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow().mapToOneOrNull(
                                Dispatchers.IO
                            ),
                        setQueries.getEMaxForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow()
                            .mapToOneOrNull(
                                Dispatchers.IO
                            ),
                        setQueries.getMaxRepsForVariation(variation.id, Instant.DISTANT_FUTURE)
                            .asFlow()
                            .mapToOneOrNull(Dispatchers.IO),
                    ) { lift, orm, emax, reps ->
                        variation.toDomain(
                            lift?.toDomain(),
                            listOfNotNull(orm, emax, reps)
                        )
                    }
                }
                merge(
                    *flows.toTypedArray()
                ).scan(emptyList()) { acc, value -> acc + value }

//                merge(
//                    *flows.toTypedArray()
//                ).scan(emptyList()) { acc, value -> acc + value }
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