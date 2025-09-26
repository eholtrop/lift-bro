package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.LBSet
import kotlinx.coroutines.flow.Flow

interface SetDataSource {
    fun listen(id: String): Flow<LBSet?>
    suspend fun save(lbSet: LBSet)
    suspend fun delete(lbSet: LBSet)
}
