package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.LBSet
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ISetRepository {

    fun listenAll(
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        variationId: String? = null,
        limit: Long = Long.MAX_VALUE
    ): Flow<List<LBSet>>

    fun listenAllForLift(
        liftId: String,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null,
        limit: Long = Long.MAX_VALUE
    ): Flow<List<LBSet>>

    fun listen(id: String): Flow<LBSet?>

    suspend fun save(lbSet: LBSet)

    suspend fun delete(lbSet: LBSet)
}
