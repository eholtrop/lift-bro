package com.lift.bro.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
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
        level = com.google.firebase.dataconnect.LogLevel.ALL // Log everything for debugging
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true // Very important for API evolution
        })
    }
}
}
