package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.createConnectionFlow
import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Category
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class KtorLiftDataSource(
    private val httpClient: HttpClient,
) : LiftDataSource {

    override fun listenAll(): Flow<List<Category>> = createConnectionFlow(httpClient, "api/ws/lifts")

    override fun get(id: String?): Flow<Category?> = createConnectionFlow(httpClient, "api/ws/lift?liftId=$id")

    override fun getAll(): List<Category> {
        TODO("NOPE")
    }

    override fun save(lift: Category): Boolean {
        GlobalScope.launch {
            httpClient.post("rest/lift") {
                contentType(ContentType.Application.Json)
                setBody(lift)
            }
        }
        return true
    }

    override suspend fun deleteAll() {
        httpClient.delete("rest/lifts")
    }

    override suspend fun delete(id: String) {
        httpClient.delete("rest/lift?id=$id")
    }
}
