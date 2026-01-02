package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.VariationId
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

enum class Sorting {
    weight, date
}

enum class Order {
    Ascending, Descending
}

interface ISetRepository {

    fun listenAll(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        variationId: String? = null,
        reps: Long? = null,
        limit: Long = Long.MAX_VALUE,
        sorting: Sorting = Sorting.date,
        order: Order = Order.Descending,
    ): Flow<List<LBSet>>

    fun listenAllForLift(
        liftId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Long = Long.MAX_VALUE,
        sorting: Sorting = Sorting.date
    ): Flow<List<LBSet>>

    fun listen(id: String): Flow<LBSet?>

    suspend fun save(lbSet: LBSet)

    suspend fun delete(lbSet: LBSet)

    suspend fun deleteAll()

    suspend fun deleteAll(variationId: VariationId)
}
