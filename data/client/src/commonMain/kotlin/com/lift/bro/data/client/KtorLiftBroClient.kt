package com.lift.bro.data.client

import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.data.core.datasource.WorkoutDataSource
import com.lift.bro.data.core.datasource.WorkoutRow
import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.get
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json

class KtorLiftBroDataSource(
    private val httpClient: HttpClient
): LiftDataSource {
    override fun listenAll(): Flow<List<Lift>> = createConnectionFlow(httpClient, "api/ws/lifts")

    override fun getAll(): List<Lift> {
        TODO("NOPE")
    }

    override fun get(id: String?): Flow<Lift?> = createConnectionFlow(httpClient, "api/ws/lifts?liftId=$id")

    override fun save(lift: Lift): Boolean {
        TODO("Not yet implemented")
    }

    override suspend fun deleteAll() {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }

}

class KtorWorkoutDataSource(
    private val httpClient: HttpClient
): WorkoutDataSource {
    override fun listenAll(startDate: LocalDate, endDate: LocalDate): Flow<List<WorkoutRow>> =
        createConnectionFlow(httpClient, "api/ws/workouts?startDate=$startDate&endDate=$endDate")

    override fun listenByDate(date: LocalDate): Flow<WorkoutRow?>

    override fun listenById(id: String): Flow<WorkoutRow?> {
        TODO("Not yet implemented")
    }

    override suspend fun save(row: WorkoutRow) {
        TODO("Not yet implemented")
    }

    override suspend fun delete(id: String) {
        TODO("Not yet implemented")
    }

}



private inline fun <reified T> createConnectionFlow(
    httpClient: HttpClient,
    path: String,
): Flow<T> {
    return flow {
        httpClient.webSocket(
            method = HttpMethod.Get,
            host = "localhost",
            port = 8080,
            path = path
        ) {
            // Listen for incoming messages
            for (frame in incoming) {
                when (frame) {
                    is Frame.Text -> emit(Json.decodeFromString<T>(frame.readText()))
                    is Frame.Close -> {
                        break
                    }
                    else -> {}
                }
            }
        }
    }
}

class KtorLiftBroClient(
    private val httpClient: HttpClient,
): LiftBroClient {
    private var baseUrl: String = "https://localhost:8080/api/"
    private var config = LiftBroClientConfig()


    override fun configure(block: LiftBroClientConfig.() -> Unit) {
        config.apply(block)
    }

    override suspend fun getHealthCheck(): Result<Map<String, Any>> {
        return safeApiCall {
            httpClient.get("$baseUrl/health").body()
        }
    }

    override fun getLifts(): Flow<List<Lift>> = createConnectionFlow("api/ws/lifts")

    override val lifts: Flow<List<Lift>>
        get() = createConnectionFlow("api/ws/lifts")

    override fun getLift(liftId: String): Flow<Lift> = createConnectionFlow("api/ws/lifts?liftId=$liftId")

    override fun getWorkouts(): Flow<List<Workout>> = createConnectionFlow("api/ws/workouts")

    override fun getExercises(): Flow<List<Exercise>> = createConnectionFlow("api/ws/exercises")

    override fun getSets(): Flow<List<LBSet>> {
        TODO("Not yet implemented")
    }

    override fun getVariations(): Flow<List<Variation>> {
        TODO("Not yet implemented")
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: Exception) {
            Result.failure(e)
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
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
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
