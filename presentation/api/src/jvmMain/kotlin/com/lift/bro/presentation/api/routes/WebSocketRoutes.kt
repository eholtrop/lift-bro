package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.*
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

fun Route.webSocketRoutes(
    webSocketManager: WebSocketManager,
    workoutDataSource: WorkoutDataSource,
    liftDataSource: LiftDataSource,
    setDataSource: SetDataSource,
    exerciseDataSource: ExerciseDataSource
) {
    val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // Stream all workouts
    webSocket("/ws/workouts") {
        val streamKey = "workouts"
        webSocketManager.subscribe(streamKey, this)
        
        try {
            // Send initial state
            val workouts = workoutDataSource.getAll()
            send(Frame.Text(json.encodeToString(workouts)))
            
            // Listen for changes and stream them
            workoutDataSource.getAllFlow()
                .onEach { updatedWorkouts ->
                    send(Frame.Text(json.encodeToString(updatedWorkouts)))
                }
                .catch { e ->
                    send(Frame.Text(json.encodeToString(mapOf("error" to e.message))))
                }
                .collect()
        } catch (e: Exception) {
            try {
                send(Frame.Text(json.encodeToString(mapOf("error" to "Connection error: ${e.message}"))))
            } catch (ignored: Exception) {
                // Connection already closed
            }
        } finally {
            webSocketManager.unsubscribe(streamKey, this)
        }
    }
    
    // Stream specific workout by ID
    webSocket("/ws/workouts/{id}") {
        val workoutId = call.parameters["id"] ?: return@webSocket close(CloseReason(CloseReason.Codes.UNSUPPORTED_DATA, "Missing workout ID"))
        val streamKey = "workouts/$workoutId"
        webSocketManager.subscribe(streamKey, this)
        
        try {
            // Send initial state
            val workout = workoutDataSource.getById(workoutId)
            send(Frame.Text(json.encodeToString(workout)))
            
            // Listen for changes
            workoutDataSource.getByIdFlow(workoutId)
                .onEach { updatedWorkout ->
                    send(Frame.Text(json.encodeToString(updatedWorkout)))
                }
                .catch { e ->
                    send(Frame.Text(json.encodeToString(mapOf("error" to e.message))))
                }
                .collect()
        } catch (e: Exception) {
            try {
                send(Frame.Text(json.encodeToString(mapOf("error" to "Connection error: ${e.message}"))))
            } catch (ignored: Exception) {
                // Connection already closed
            }
        } finally {
            webSocketManager.unsubscribe(streamKey, this)
        }
    }
    
    // Stream lifts with optional workout filter
    webSocket("/ws/lifts") {
        val workoutId = call.request.queryParameters["workoutId"]
        val streamKey = if (workoutId != null) "lifts?workoutId=$workoutId" else "lifts"
        webSocketManager.subscribe(streamKey, this)
        
        try {
            // Send initial state
            val lifts = if (workoutId != null) {
                liftDataSource.getByWorkoutId(workoutId)
            } else {
                liftDataSource.getAll()
            }
            send(Frame.Text(json.encodeToString(lifts)))
            
            // Listen for changes
            val flow = if (workoutId != null) {
                liftDataSource.getByWorkoutIdFlow(workoutId)
            } else {
                liftDataSource.getAllFlow()
            }
            
            flow.onEach { updatedLifts ->
                    send(Frame.Text(json.encodeToString(updatedLifts)))
                }
                .catch { e ->
                    send(Frame.Text(json.encodeToString(mapOf("error" to e.message))))
                }
                .collect()
        } catch (e: Exception) {
            try {
                send(Frame.Text(json.encodeToString(mapOf("error" to "Connection error: ${e.message}"))))
            } catch (ignored: Exception) {
                // Connection already closed
            }
        } finally {
            webSocketManager.unsubscribe(streamKey, this)
        }
    }
    
    // Stream sets with optional lift filter
    webSocket("/ws/sets") {
        val liftId = call.request.queryParameters["liftId"]
        val streamKey = if (liftId != null) "sets?liftId=$liftId" else "sets"
        webSocketManager.subscribe(streamKey, this)
        
        try {
            // Send initial state
            val sets = if (liftId != null) {
                setDataSource.getByLiftId(liftId)
            } else {
                setDataSource.getAll()
            }
            send(Frame.Text(json.encodeToString(sets)))
            
            // Listen for changes
            val flow = if (liftId != null) {
                setDataSource.getByLiftIdFlow(liftId)
            } else {
                setDataSource.getAllFlow()
            }
            
            flow.onEach { updatedSets ->
                    send(Frame.Text(json.encodeToString(updatedSets)))
                }
                .catch { e ->
                    send(Frame.Text(json.encodeToString(mapOf("error" to e.message))))
                }
                .collect()
        } catch (e: Exception) {
            try {
                send(Frame.Text(json.encodeToString(mapOf("error" to "Connection error: ${e.message}"))))
            } catch (ignored: Exception) {
                // Connection already closed
            }
        } finally {
            webSocketManager.unsubscribe(streamKey, this)
        }
    }
    
    // Stream all exercises
    webSocket("/ws/exercises") {
        val streamKey = "exercises"
        webSocketManager.subscribe(streamKey, this)
        
        try {
            // Send initial state
            val exercises = exerciseDataSource.getAll()
            send(Frame.Text(json.encodeToString(exercises)))
            
            // Listen for changes
            exerciseDataSource.getAllFlow()
                .onEach { updatedExercises ->
                    send(Frame.Text(json.encodeToString(updatedExercises)))
                }
                .catch { e ->
                    send(Frame.Text(json.encodeToString(mapOf("error" to e.message))))
                }
                .collect()
        } catch (e: Exception) {
            try {
                send(Frame.Text(json.encodeToString(mapOf("error" to "Connection error: ${e.message}"))))
            } catch (ignored: Exception) {
                // Connection already closed
            }
        } finally {
            webSocketManager.unsubscribe(streamKey, this)
        }
    }
}