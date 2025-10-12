package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.repositories.ISetRepository
import kotlinx.coroutines.flow.Flow

class SetRepository(
    private val local: SetDataSource,
): ISetRepository {

    override fun listen(id: String): Flow<LBSet?> = local.listen(id)

    override fun listenAll(): Flow<List<LBSet>> = local.listenAll()

    override suspend fun save(lbSet: LBSet) = local.save(lbSet)

    override suspend fun delete(lbSet: LBSet) = local.delete(lbSet)
}
