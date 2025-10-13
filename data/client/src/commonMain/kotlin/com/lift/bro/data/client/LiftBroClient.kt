package com.lift.bro.data.client

import io.ktor.client.HttpClient

/**
 * Configuration class for the Lift Bro client
 */
data class LiftBroClientConfig(
    var connectTimeoutMs: Long = 30_000,
    var requestTimeoutMs: Long = 15_000,
    var socketTimeoutMs: Long = 15_000,
    var enableLogging: Boolean = false,
    var retryOnConnectionFailure: Boolean = true,
    var maxRetryAttempts: Int = 3
)

/**
 * Platform-specific client factory function
 */
expect fun createLiftBroClient(): HttpClient
