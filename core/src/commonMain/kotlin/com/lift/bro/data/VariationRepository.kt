package com.lift.bro.data

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.utils.mapEach
import comliftbrodb.LiftQueries
import comliftbrodb.VariationQueries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class VariationRepository(

    private val liftQueries: LiftQueries,
    private val variationQueries: VariationQueries,
) : IVariationRepository {

    override suspend fun deleteAll() {
        variationQueries.deleteAll()
    }

    override fun save(id: String, liftId: String, name: String?) {
        GlobalScope.launch {
            variationQueries.save(id = id, liftId = liftId, name = name)
        }
    }

    override fun getAll(liftId: String): List<Variation> {
        val parentLift = liftQueries.get(liftId).executeAsOneOrNull()?.toDomain()

        return variationQueries.getAllForLift(liftId).executeAsList().map { it.toDomain(parentLift!!) }
    }

    override fun getAll(): List<Variation> {
        val parentLift = liftQueries.getAll().executeAsList().map { it.toDomain() }
        return variationQueries.getAll().executeAsList().map { variation -> variation.toDomain(parentLift.first { it.id == variation.liftId }) }
    }

    override fun listenAll(liftId: String): Flow<List<Variation>> {
        val parentLift = liftQueries.get(liftId).executeAsOne().toDomain()
        return variationQueries.getAllForLift(liftId).asFlow().mapToList(Dispatchers.IO)
            .mapEach { it.toDomain(parentLift) }
    }

    override fun listenAll(): Flow<List<Variation>> {
        return variationQueries.getAll().asFlow().mapToList(Dispatchers.IO).map { variations ->
            val lifts = liftQueries.getAll().executeAsList().map { it.toDomain() }
            variations.map { variation -> variation.toDomain(lifts.first{ it.id == variation.liftId }) }
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

        return variation?.toDomain(lift?.toDomain()!!)
    }

}