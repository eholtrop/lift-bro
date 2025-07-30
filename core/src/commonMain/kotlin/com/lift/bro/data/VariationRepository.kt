package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.mapEach
import comliftbrodb.LiftQueries
import comliftbrodb.SetQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VariationRepository(
    private val liftQueries: LiftQueries,
    private val setQueries: SetQueries,
    private val variationQueries: VariationQueries,
) : IVariationRepository {

    override suspend fun deleteAll() {
        variationQueries.deleteAll()
    }

    override fun save(id: String, liftId: String, name: String?) {
        GlobalScope.launch {
            variationQueries.save(id = id, liftId = liftId, name = name, "", 0L)
        }
    }

    // only used for backup, will not populate emax/max
    override fun getAll(): List<Variation> {
        val parentLift = liftQueries.getAll().executeAsList().map { it.toDomain() }
        return variationQueries.getAll().executeAsList().map { variation ->
            variation.toDomain(
                parentLift.first { it.id == variation.liftId },
                emptyList()
            )
        }
    }

    override fun listenAll(liftId: String): Flow<List<Variation>> {
        return combine(
            liftQueries.get(liftId).asFlow().mapToOneOrNull(Dispatchers.IO).map { it?.toDomain() },
            variationQueries.getAllForLift(liftId).asFlow().mapToList(Dispatchers.IO),
            setQueries.getAll().asFlow().mapToList(Dispatchers.IO)
        ) { lift, variations, sets ->
            variations.map { variation ->
                variation.toDomain(
                    parentLift = lift,
                    sets = sets.filter { it.variationId == variation.id }
                )
            }
        }
    }

    override fun listenAll(): Flow<List<Variation>> {
        return combine(
            variationQueries.getAll().asFlow().mapToList(Dispatchers.IO),
            setQueries.getAll().asFlow().mapToList(Dispatchers.IO)
        ) { variations, sets ->
            variations.map { variation ->
                variation.toDomain(
                    parentLift = liftQueries.get(variation.liftId).executeAsOneOrNull()?.toDomain(),
                    sets = sets.filter { it.variationId == variation.id }
                )
            }
        }
    }

    override fun delete(id: String) {
        GlobalScope.launch {
            variationQueries.delete(id)
        }
    }

    override fun get(variationId: String?): Variation? {
        val variation = variationQueries.get(variationId ?: "").executeAsOneOrNull()
        val lift = liftQueries.get(variation?.liftId ?: "").executeAsOneOrNull()
        val sets = setQueries.getAllByVariation(variationId ?: "").executeAsList()

        return variation?.toDomain(
            lift?.toDomain(),
            sets
        )
    }

}