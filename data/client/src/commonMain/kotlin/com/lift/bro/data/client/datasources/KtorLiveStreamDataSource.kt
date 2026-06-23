package com.lift.bro.data.client.datasources

import com.lift.bro.data.client.LiftBroClientConfig
import com.lift.bro.data.client.createLiftBroClient
import com.lift.bro.data.core.datasource.LiveStreamDataSource
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class KtorLiveStreamDataSource(
    private val httpClient: HttpClient = createLiftBroClient(
        LiftBroClientConfig(baseUrl = "https://decapi.me")
    )
) : LiveStreamDataSource {
    override suspend fun isLive(channelName: String): Boolean {
        val response = httpClient.get("/twitch/uptime/$channelName")
        return when (response.status) {
            HttpStatusCode.OK -> !response.bodyAsText().contains("offline")
            else -> {
                false
            }
        }
    }
}
