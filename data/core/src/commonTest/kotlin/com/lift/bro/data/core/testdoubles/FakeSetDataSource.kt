package com.lift.bro.data.core.testdoubles

import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.MovementId
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class FakeSetDataSource(
    private val sets: List<LBSet> = emptyList()
) : SetDataSource {
    var lastStartDate: LocalDate? = null
        private set
    var lastEndDate: LocalDate? = null
        private set
    var lastVariationId: String? = null
        private set
    var lastReps: Long? = null
        private set
    var lastLimit: Long? = null
        private set
    var lastSorting: Sorting? = null
        private set
    var lastOrder: Order? = null
        private set
    var lastLiftId: String? = null
        private set
    var lastId: String? = null
        private set
    var savedSet: LBSet? = null
        private set
    var deletedSet: LBSet? = null
        private set
    var deleteAllCalled = false
        private set
    var deletedVariationId: MovementId? = null
        private set

    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
        reps: Long?,
        sorting: Sorting,
        order: Order
    ): Flow<List<LBSet>> {
        lastStartDate = startDate
        lastEndDate = endDate
        lastVariationId = variationId
        lastReps = reps
        lastLimit = limit
        lastSorting = sorting
        lastOrder = order
        return flowOf(sets)
    }

    override fun listenAllForLift(
        liftId: String?,
        limit: Long,
        sorting: Sorting
    ): Flow<List<LBSet>> {
        lastLiftId = liftId
        lastLimit = limit
        lastSorting = sorting
        return flowOf(sets)
    }

    override fun listen(id: String): Flow<LBSet?> {
        lastId = id
        return flowOf(sets.firstOrNull { it.id == id })
    }

    override suspend fun save(lbSet: LBSet) {
        savedSet = lbSet
    }

    override suspend fun delete(lbSet: LBSet) {
        deletedSet = lbSet
    }

    override suspend fun deleteAll() {
        deleteAllCalled = true
    }

    override suspend fun deleteAll(variationId: MovementId) {
        deletedVariationId = variationId
    }
}

fun fakeSetDataSource(
    sets: List<LBSet> = emptyList()
): FakeSetDataSource = FakeSetDataSource(sets)
