package com.lift.bro.data.client.datasources


import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Lift
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class KtorLiftDataSource(
    private val httpClient: HttpClient = createLiftBroClient(),
): LiftDataSource {

    override fun listenAll(): Flow<List<Lift>> = createConnectionFlow(httpClient, "api/ws/lifts")

    override fun get(id: String?): Flow<Lift?> = createConnectionFlow(httpClient, "api/ws/lifts?liftId=$id")

    override fun getAll(): List<Lift> {
        TODO("NOPE")
    }

    override fun save(lift: Lift): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }
}
