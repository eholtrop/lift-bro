package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.LiftBroClientConfig
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.logging.Log
import com.lift.bro.logging.d
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode

class KtorServerHealthDataSource(
    url: String
) {
    private val httpClient: HttpClient = createLiftBroClient(
        LiftBroClientConfig(
            baseUrl = url
        )
    )

    suspend fun check(): Boolean {
        with(httpClient.get("/health").call) {
            Log.d(message = "Response: $response")
            return response.status == HttpStatusCode.OK
        }
    }
}
