package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.WorkoutDataSource
import com.lift.bro.domain.models.Workout
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.workoutRestRoutes(
    workoutDataSource: WorkoutDataSource,
    webSocketManager: WebSocketManager
) {
    route("/workouts") {
        
        // GET /workouts - Get all workouts
        get {
            try {
                val workouts = workoutDataSource.getAll()
                call.respond(HttpStatusCode.OK, workouts)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // GET /workouts/{id} - Get specific workout
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Missing workout ID")
            )
            
            try {
                val workout = workoutDataSource.getById(id)
                if (workout != null) {
                    call.respond(HttpStatusCode.OK, workout)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // POST /workouts - Create new workout
        post {
            try {
                val workout = call.receive<Workout>()
                val createdWorkout = workoutDataSource.create(workout)
                
                // Emit update to WebSocket subscribers
                webSocketManager.emitWorkoutUpdate(createdWorkout)
                
                call.respond(HttpStatusCode.Created, createdWorkout)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // PUT /workouts/{id} - Update workout
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing workout ID")
            )
            
            try {
                val workout = call.receive<Workout>()
                
                // Ensure the ID matches
                val workoutWithId = workout.copy(id = id)
                
                val updatedWorkout = workoutDataSource.update(workoutWithId)
                if (updatedWorkout != null) {
                    // Emit update to WebSocket subscribers
                    webSocketManager.emitWorkoutUpdate(updatedWorkout)
                    
                    call.respond(HttpStatusCode.OK, updatedWorkout)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // DELETE /workouts/{id} - Delete workout
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing workout ID")
            )
            
            try {
                val deleted = workoutDataSource.delete(id)
                if (deleted) {
                    // Emit update to WebSocket subscribers (deleted workout)
                    webSocketManager.emitWorkoutUpdate(mapOf("deleted" to id))
                    
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Workout not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}