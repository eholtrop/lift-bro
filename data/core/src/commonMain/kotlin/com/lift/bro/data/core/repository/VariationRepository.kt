package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Movement
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class VariationRepository(
    private val local: VariationDataSource
) : IVariationRepository {

    override fun listenAll(liftId: String?): Flow<List<Movement>> = local.listenAllForLift(liftId)
    override fun listenAll(): Flow<List<Movement>> = local.listenAll()

    override fun listen(id: String): Flow<Movement?> = local.listen(id)

    override fun get(variationId: String?): Movement? = local.get(variationId ?: "")

    override fun getAll(): List<Movement> = local.getAll()

    override fun delete(id: String) {
        GlobalScope.launch {
            local.delete(id)
        }
    }

    override suspend fun deleteAll() {
        local.deleteAll()
    }

    override fun save(variation: Movement) {
        GlobalScope.launch { local.save(variation) }
    }
}
