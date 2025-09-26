package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.IVariationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class VariationRepository(
    private val local: VariationDataSource
) : IVariationRepository {

    override fun listenAll(liftId: String): Flow<List<Variation>> = local.listenAllForLift(liftId)
    override fun listenAll(): Flow<List<Variation>> = local.listenAll()

    override fun listen(id: String): Flow<Variation?> = local.listen(id)

    override fun get(variationId: String?): Variation? = local.get(variationId ?: "")

    override fun getAll(): List<Variation> = local.getAll()

    override fun delete(id: String) { /* expose as suspend in future if needed */
        // For now, delegate to suspend via fire-and-forget is not ideal; prefer making repository API suspend.
        // Keeping signature to match existing interface.
        // Call sites should migrate to suspend.
        // Provide a best-effort bridge:
        GlobalScope.launch {
            local.delete(id)
        }
    }

    override suspend fun deleteAll() {
        local.deleteAll()
    }

    override fun save(variation: Variation) {
        GlobalScope.launch { local.save(variation) }
    }
}
