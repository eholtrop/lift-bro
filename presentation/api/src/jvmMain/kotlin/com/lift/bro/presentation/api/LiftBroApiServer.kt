package com.lift.bro.presentation.api

import com.lift.bro.data.LBDatabase
import com.lift.bro.data.core.datasource.*
import com.lift.bro.data.sqldelight.datasource.*
import com.lift.bro.presentation.api.routes.configureRouting
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.websocket.*
import kotlinx.serialization.json.Json
import java.time.Duration

class LiftBroApiServer(
    private val database: LBDatabase,
    private val port: Int = 8080
) {
    private var server: ApplicationEngine? = null
    private val webSocketManager = WebSocketManager()
    
    // Data sources
    private val workoutDataSource: WorkoutDataSource by lazy {
        SqldelightWorkoutDataSource(database.workoutQueries)
    }
    private val liftDataSource: LiftDataSource by lazy {
        SqldelightLiftDataSource(database.liftQueries)
    }
    private val setDataSource: SetDataSource by lazy {
        SqldelightSetDataSource(database.setQueries)
    }
    private val exerciseDataSource: ExerciseDataSource by lazy {
        SqldelightExerciseDataSource(database.exerciseQueries, database.setQueries, database.variationQueries)
    }
    private val settingsDataSource: SettingsDataSource by lazy {
        // Note: You'll need to implement this based on your settings structure
        TODO("Implement settings data source")
    }
    
    fun start() {
        if (server?.isActive == true) return
        
        server = embeddedServer(CIO, port = port, host = "0.0.0.0") {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            
            install(WebSockets) {
                pingPeriod = Duration.ofSeconds(15)
                timeout = Duration.ofSeconds(15)
                maxFrameSize = Long.MAX_VALUE
                masking = false
            }
            
            configureRouting(
                workoutDataSource = workoutDataSource,
                liftDataSource = liftDataSource,
                setDataSource = setDataSource,
                exerciseDataSource = exerciseDataSource,
                webSocketManager = webSocketManager
            )
        }.start(wait = false)
        
        println("LiftBro API Server started at http://0.0.0.0:$port")
    }
    
    fun stop() {
        server?.stop(gracePeriodMillis = 1000, timeoutMillis = 5000)
        server = null
        webSocketManager.closeAllConnections()
        println("LiftBro API Server stopped")
    }
    
    fun isRunning(): Boolean = server?.isActive == true
}