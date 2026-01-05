package com.lift.bro.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

actual fun createLiftBroClient(): io.ktor.client.HttpClient {
    return HttpClient(Darwin) {
        // Engine-specific configuration
        engine {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        }

        // Common configuration (plugins)
        install(Logging) {
            level = LogLevel.ALL
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
    }
}
