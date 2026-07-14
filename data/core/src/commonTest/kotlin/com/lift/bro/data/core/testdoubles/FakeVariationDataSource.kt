package com.lift.bro.data.core.testdoubles

import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Movement
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeVariationDataSource(
    private val variations: List<Movement> = emptyList(),
    private val singleVariation: Movement? = null
) : VariationDataSource {
    var lastId: String? = null
        private set
    var lastLiftId: String? = null
        private set
    var savedVariation: Movement? = null
        private set
    var deleteAllCalled = false
        private set

    override fun listen(id: String): Flow<Movement?> {
        lastId = id
        return flowOf(singleVariation ?: variations.firstOrNull { it.id == id })
    }

    override fun listenAll(): Flow<List<Movement>> = flowOf(variations)

    override fun listenAllForLift(liftId: String?): Flow<List<Movement>> {
        lastLiftId = liftId
        return flowOf(variations)
    }

    override fun get(id: String): Movement? {
        lastId = id
        return singleVariation ?: variations.firstOrNull { it.id == id }
    }

    override fun getAll(): List<Movement> = variations

    override suspend fun save(variation: Movement) {
        savedVariation = variation
    }

    override suspend fun delete(id: String) {
        lastId = id
    }

    override suspend fun deleteAll() {
        deleteAllCalled = true
    }
}

fun fakeVariationDataSource(
    variations: List<Movement> = emptyList(),
    singleVariation: Movement? = null
): FakeVariationDataSource = FakeVariationDataSource(variations, singleVariation)
