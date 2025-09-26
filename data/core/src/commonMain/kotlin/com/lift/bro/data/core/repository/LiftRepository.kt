package com.lift.bro.data.core.repository

import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.repositories.ILiftRepository
import kotlinx.coroutines.flow.Flow

class LiftRepository(
private val local: LiftDataSource
) : ILiftRepository {
    override fun listenAll(): Flow<List<Lift>> = local.listenAll()
    override fun getAll(): List<Lift> = local.getAll()
    override fun get(id: String?): Flow<Lift?> = local.get(id)
    override fun save(lift: Lift): Boolean = local.save(lift)
    override suspend fun deleteAll() = local.deleteAll()
    override suspend fun delete(id: String) = local.delete(id)
}
