package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.*
import com.lift.bro.domain.repositories.IExerciseRepository
import com.lift.bro.domain.repositories.ILiftRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.domain.repositories.IVariationRepository
import com.lift.bro.domain.repositories.IWorkoutRepository
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
    setRepository: ISetRepository,
    liftRepository: ILiftRepository,
    variationRepository: IVariationRepository,
    exerciseRepository: IExerciseRepository,
    workoutRepository: IWorkoutRepository
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
            workoutRepository.getAll()
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
        val workoutId = call.parameters["id"] ?: return@webSocket close(CloseReason(CloseReason.Codes.CANNOT_ACCEPT, "Missing workout ID"))
        val streamKey = "workouts/$workoutId"
        webSocketManager.subscribe(streamKey, this)

        try {
            // Listen for changes
            workoutRepository.get(workoutId)
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
            liftRepository.listenAll()
                .onEach { updatedLifts ->
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

    // Stream lifts with optional workout filter
    webSocket("/ws/variations") {
        val streamKey = "variations"
        webSocketManager.subscribe(streamKey, this)

        try {
            variationRepository.listenAll()
                .onEach { updatedLifts ->
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
            setRepository.listenAll()
                .onEach { updatedSets ->
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
}
