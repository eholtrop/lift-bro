package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.KtorLiftBroClient
import com.lift.bro.data.client.LiftBroClient
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Lift
import kotlinx.coroutines.flow.Flow

class KtorLiftDataSource(
    private val client: LiftBroClient = createLiftBroClient()
): LiftDataSource {
    override fun listenAll(): Flow<List<Lift>> = client.getLifts()

    override fun getAll(): List<Lift> {
        TODO("Will not be implemented, instead refactor")
    }

    override fun get(id: String?): Flow<Lift?> = client.getLift(liftId = id)


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
