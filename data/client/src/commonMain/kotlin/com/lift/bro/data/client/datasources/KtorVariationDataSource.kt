package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Variation
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

class KtorVariationDataSource(
    private val httpClient: HttpClient,
): VariationDataSource {

    override fun listen(id: String): Flow<Variation?> = createConnectionFlow(
        httpClient,
        "api/ws/variation?variationId=$id"
    )

    override fun listenAll(): Flow<List<Variation>> = createConnectionFlow(httpClient, "api/ws/variations")

    override fun listenAllForLift(liftId: String?): Flow<List<Variation>> = createConnectionFlow(
        httpClient,
        "api/ws/variations?liftId=$liftId"
    )

    override suspend fun save(variation: Variation) {
        httpClient.post("api/rest/variation") {
            contentType(ContentType.Application.Json)
            setBody(variation)
        }
    }

    override suspend fun delete(id: String) {
        httpClient.delete("api/rest/variation?id=$id")
    }

    override suspend fun deleteAll() {
        httpClient.delete("api/rest/variations")
    }

    override fun get(id: String): Variation? {
        TODO("NOPE")
    }

    override fun getAll(): List<Variation> {
        TODO("NOPE")
    }
}
