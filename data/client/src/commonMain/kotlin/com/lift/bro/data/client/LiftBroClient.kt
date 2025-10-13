package com.lift.bro.data.client

import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import kotlinx.coroutines.flow.Flow

/**
 * Main client interface for communicating with the Lift Bro server
 * Supports both REST API calls and WebSocket connections
 */
interface LiftBroClient {
    // REST API methods
    suspend fun getHealthCheck(): Result<Map<String, Any>>

    val lifts: Flow<List<Lift>>

    fun getLifts(): Flow<List<Lift>>
    fun getLift(liftId: String): Flow<Lift?>

    fun getWorkouts(): Flow<List<Workout>>
    fun getExercises(): Flow<List<Exercise>>
    fun getSets(): Flow<List<LBSet>>
    fun getVariations(): Flow<List<Variation>>

    // Configuration
    fun configure(block: LiftBroClientConfig.() -> Unit)
}

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
expect fun createLiftBroClient(): LiftBroClient
