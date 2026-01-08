package com.lift.bro.data.client

import io.ktor.client.HttpClient

/**
 * Configuration class for the Lift Bro client
 */
data class LiftBroClientConfig(
    val baseUrl: String = "http://localhost",
    val connectTimeoutMs: Long = 30_000,
    val requestTimeoutMs: Long = 15_000,
    val socketTimeoutMs: Long = 15_000,
    val enableLogging: Boolean = false,
    val retryOnConnectionFailure: Boolean = true,
    val maxRetryAttempts: Int = 3
)

/**
 * Platform-specific client factory function
 */
expect fun createLiftBroClient(
    config: LiftBroClientConfig = LiftBroClientConfig()
): HttpClient
