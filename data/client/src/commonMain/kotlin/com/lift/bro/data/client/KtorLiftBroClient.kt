package com.lift.bro.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.http.HttpMethod
import io.ktor.http.URLProtocol
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json

internal inline fun <reified T> createConnectionFlow(
    httpClient: HttpClient,
    path: String,
): Flow<T> {
    return flow {
        Log.d("LiftBroClient", "creating connection flow $path")
        try {
            httpClient.webSocket(
                method = HttpMethod.Get,
                path = path
            ) {
                // Listen for incoming messages
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> {
                            Log.d("LiftBroClient", frame.readText())
                            emit(Json.decodeFromString<T>(frame.readText()))
                        }
                        is Frame.Close -> {
                            break
                        }

                        else -> {}
                    }
                }
            }
        } catch (e: Exception) {
            Log.d("LiftBroClient", "creating connection failed $path")
            e.printStackTrace()
        }
    }
}

/**
 * Creates a configured HttpClient for the platform
 */
internal fun createConfiguredHttpClient(
    platformEngine: HttpClientEngine,
    config: LiftBroClientConfig,
): HttpClient {
    return HttpClient(platformEngine) {
        defaultRequest {
            url {
                protocol = if (config.baseUrl.contains("https")) URLProtocol.HTTPS else URLProtocol.HTTP
                host = config.baseUrl.removePrefix("http://").removePrefix("https://")
            }
        }
        install(ContentNegotiation) {
            json(
                Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                }
            )
        }

        install(HttpTimeout) {
            requestTimeoutMillis = config.requestTimeoutMs
            connectTimeoutMillis = config.connectTimeoutMs
            socketTimeoutMillis = config.socketTimeoutMs
        }

        install(WebSockets) {
            pingInterval = kotlin.time.Duration.parse("20s")
        }

        if (config.enableLogging) {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
        }

        // Add retry logic if enabled
        if (config.retryOnConnectionFailure) {
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = config.maxRetryAttempts)
                exponentialDelay()
            }
        }
    }
}
