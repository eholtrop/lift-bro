package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.LBSet
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SetDataSource {

    fun listenAll(startDate: LocalDate?, endDate: LocalDate?, variationId: String?, limit: Long): Flow<List<LBSet>>
    fun listen(id: String): Flow<LBSet?>
    suspend fun save(lbSet: LBSet)
    suspend fun delete(lbSet: LBSet)

}
