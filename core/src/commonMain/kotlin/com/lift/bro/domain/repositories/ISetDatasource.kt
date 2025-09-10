package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.LBSet
import kotlinx.coroutines.flow.Flow

interface ISetDatasource {

    fun listen(id: String): Flow<LBSet?>

    suspend fun save(lbSet: LBSet)

    suspend fun delete(lbSet: LBSet)
}