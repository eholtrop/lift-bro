package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.*
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(
    workoutDataSource: WorkoutDataSource,
    liftDataSource: LiftDataSource,
    setDataSource: SetDataSource,
    exerciseDataSource: ExerciseDataSource,
    webSocketManager: WebSocketManager
) {
    routing {
        // WebSocket streaming endpoints
        webSocketRoutes(webSocketManager, workoutDataSource, liftDataSource, setDataSource, exerciseDataSource)
        
        // REST CRUD endpoints
        workoutRestRoutes(workoutDataSource, webSocketManager)
        liftRestRoutes(liftDataSource, webSocketManager)
        setRestRoutes(setDataSource, webSocketManager)
        exerciseRestRoutes(exerciseDataSource, webSocketManager)
    }
}