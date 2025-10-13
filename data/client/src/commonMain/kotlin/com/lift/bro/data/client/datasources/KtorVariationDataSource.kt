package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.VariationDataSource
import com.lift.bro.domain.models.Variation
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class KtorVariationDataSource(
    private val httpClient: HttpClient,
): VariationDataSource {
    override suspend fun save(variation: Variation) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override fun listen(id: String): Flow<Variation?> = createConnectionFlow(httpClient, "api/ws/variations?variationId=$id")

    override fun listenAll(): Flow<List<Variation>> = createConnectionFlow(httpClient, "api/ws/variations")

    override fun listenAllForLift(liftId: String?): Flow<List<Variation>> = createConnectionFlow(httpClient, "api/ws/variations?liftId=$liftId")

    override fun get(id: String): Variation? {
        TODO("Not yet implemented")
    }

    override fun getAll(): List<Variation> {
        TODO("Not yet implemented")
    }

}
