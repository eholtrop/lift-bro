package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.LiftBroClientConfig
import com.lift.bro.data.client.Log
import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.data.client.d
import com.lift.bro.domain.models.Goal
import com.lift.bro.domain.repositories.IGoalRepository
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow

class KtorGoalRepository(
    val baseUrl: String,
): IGoalRepository {

    private val httpClient = createLiftBroClient(config = LiftBroClientConfig(baseUrl = baseUrl))

    override fun get(id: String): Flow<Goal?> = createConnectionFlow(httpClient, "api/ws/goal?goalId=$id")

    override fun getAll(): Flow<List<Goal>> = createConnectionFlow(httpClient, "api/ws/goals")

    override suspend fun save(goal: Goal) {
        Log.d("LiftBroClient", "saving goal $goal")
        httpClient.post("api/rest/goal") {
            contentType(ContentType.Application.Json)
            setBody(goal)
        }
    }

    override suspend fun delete(goal: Goal) {
        Log.d("LiftBroClient", "deleting goal $goal")
        httpClient.delete("api/rest/goal?id=${goal.id}")
    }
}
