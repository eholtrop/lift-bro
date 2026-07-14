@file:Suppress("detekt:EmptyFunctionBlock")

package com.lift.bro.testdoubles

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate

class FakeSetRepository(
    private val sets: List<LBSet> = emptyList()
) : ISetRepository {
    var lastStartDate: LocalDate? = null
        private set
    var lastEndDate: LocalDate? = null
        private set
    var lastVariationId: String? = null
        private set

    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        reps: Long?,
        limit: Long,
        sorting: Sorting,
        order: Order
    ): Flow<List<LBSet>> {
        lastStartDate = startDate
        lastEndDate = endDate
        lastVariationId = variationId
        return flowOf(sets)
    }

    override fun listenAllForLift(
        liftId: String?,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Long,
        sorting: Sorting
    ): Flow<List<LBSet>> = flowOf(sets)

    override fun listen(id: String): Flow<LBSet?> = flowOf(null)
    override suspend fun save(lbSet: LBSet) {}
    override suspend fun delete(lbSet: LBSet) {}
    override suspend fun deleteAll() {}
    override suspend fun deleteAll(variationId: String) {}
}
