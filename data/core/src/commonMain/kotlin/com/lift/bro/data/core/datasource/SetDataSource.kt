package com.lift.bro.data.core.datasource

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface SetDataSource {

    fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
        sorting: Sorting = Sorting.date,
        order: Order = Order.Descending,
    ): Flow<List<LBSet>>

    fun listenAllForLift(
        liftId: String,
        limit: Long = Long.MAX_VALUE,
        sorting: Sorting = Sorting.date,
    ): Flow<List<LBSet>>

    fun listen(id: String): Flow<LBSet?>
    suspend fun save(lbSet: LBSet)
    suspend fun delete(lbSet: LBSet)
    suspend fun deleteAll()
    suspend fun deleteAll(variationId: VariationId)

}
