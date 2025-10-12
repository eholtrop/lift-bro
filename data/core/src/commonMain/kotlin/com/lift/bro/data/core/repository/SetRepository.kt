package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.ISetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class SetRepository(
    private val local: SetDataSource,
): ISetRepository {

    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
    ): Flow<List<LBSet>> = local.listenAll(startDate, endDate, variationId, limit)

    override fun listenAllForLift(
        liftId: String,
        startDate: LocalDate?,
        endDate: LocalDate?,
        limit: Long
    ): Flow<List<LBSet>> = local.listenAllForLift(liftId, limit)

    override fun listen(id: String): Flow<LBSet?> = local.listen(id)
    override suspend fun save(lbSet: LBSet) = local.save(lbSet)
    override suspend fun delete(lbSet: LBSet) = local.delete(lbSet)
}
