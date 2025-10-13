package com.lift.bro.presentation.server

import com.lift.bro.domain.server.LiftBroServer
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
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
            call.respond(mapOf("status" to "healthy", "timestamp" to System.currentTimeMillis()))
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
