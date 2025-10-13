package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.VariationId
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate


class KtorSetDataSource(
    private val httpClient: HttpClient,
): SetDataSource {
    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
    ): Flow<List<LBSet>> = createConnectionFlow(httpClient, "api/ws/sets?limit=$limit&startDate=$startDate&endDate=$endDate&variationId=$variationId")

    override fun listenAllForLift(
        liftId: String,
        limit: Long,
    ): Flow<List<LBSet>> = createConnectionFlow(httpClient, "api/ws/sets?limit=$limit&liftId=$liftId")

    override fun listen(id: String): Flow<LBSet?> = createConnectionFlow(httpClient, "api/ws/sets?setId=$id")

    override suspend fun save(lbSet: LBSet) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(lbSet: LBSet) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll(variationId: VariationId) {
        TODO("Not yet implemented")
    }
}
