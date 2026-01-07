package com.lift.bro.presentation.server

import com.lift.bro.domain.server.LiftBroServer
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.ktor.server.websocket.WebSockets
import kotlinx.serialization.json.Json

/**
 * Common server configuration that works across all platforms
 */
fun Application.configureLiftBroApp() {
    install(WebSockets) {
        pingPeriodMillis = 15_000
        timeoutMillis = 15_000
    }
    configureSerialization()
    configureRouting()
}

fun Application.configureSerialization() {
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

fun Application.configureRouting() {
    routing {
        get("/") {
            println("LiftBroServer: Received request to /")
            call.respond(mapOf("message" to "Lift Bro API", "version" to "0.0.0"))
        }

        get("/health") {
            println("LiftBroServer: Received request to /health")
            try {
                call.respond(mapOf("status" to "healthy", "timestamp" to System.currentTimeMillis().toString()))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        route("/api") {
            configureApiRoutes()
        }
    }
}

/**
 * Platform-specific server factory function
 */
expect fun createLiftBroServer(): LiftBroServer
