package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.VariationId
import com.lift.bro.domain.repositories.Order
import com.lift.bro.domain.repositories.Sorting
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

class KtorSetDataSource(
    private val httpClient: HttpClient,
) : SetDataSource {

    override fun listenAll(
        startDate: LocalDate?,
        endDate: LocalDate?,
        variationId: String?,
        limit: Long,
        reps: Long?,
        sorting: Sorting,
        order: Order,
    ): Flow<List<LBSet>> = createConnectionFlow(
        httpClient,
        "api/ws/sets?limit=$limit&startDate=$startDate&endDate=$endDate&variationId=$variationId"
    )

    override fun listenAllForLift(
        liftId: String,
        limit: Long,
        sorting: Sorting
    ): Flow<List<LBSet>> = createConnectionFlow(httpClient, "api/ws/sets?limit=$limit&liftId=$liftId")

    override fun listen(id: String): Flow<LBSet?> = createConnectionFlow(httpClient, "api/ws/sets?setId=$id")

    override suspend fun save(lbSet: LBSet) {
        httpClient.post("api/rest/sets") {
            contentType(ContentType.Application.Json)
            setBody(lbSet)
        }
    }

    override suspend fun delete(lbSet: LBSet) {
        httpClient.delete("api/rest/sets?id=${lbSet.id}")
    }

    override suspend fun deleteAll() {
        httpClient.delete("api/rest/sets")
    }

    override suspend fun deleteAll(variationId: VariationId) {
        httpClient.delete("api/rest/sets?variationId=${variationId}")
    }
}
