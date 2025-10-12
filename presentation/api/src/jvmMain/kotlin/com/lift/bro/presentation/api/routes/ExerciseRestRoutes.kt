package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.ExerciseDataSource
import com.lift.bro.domain.models.Exercise
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.exerciseRestRoutes(
    exerciseDataSource: ExerciseDataSource,
    webSocketManager: WebSocketManager
) {
    route("/exercises") {
        
        // GET /exercises - Get all exercises
        get {
            try {
                val exercises = exerciseDataSource.getAll()
                call.respond(HttpStatusCode.OK, exercises)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // GET /exercises/{id} - Get specific exercise
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Missing exercise ID")
            )
            
            try {
                val exercise = exerciseDataSource.getById(id)
                if (exercise != null) {
                    call.respond(HttpStatusCode.OK, exercise)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Exercise not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // POST /exercises - Create new exercise
        post {
            try {
                val exercise = call.receive<Exercise>()
                val createdExercise = exerciseDataSource.create(exercise)
                
                // Emit update to WebSocket subscribers
                webSocketManager.emitExerciseUpdate(createdExercise)
                
                call.respond(HttpStatusCode.Created, createdExercise)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // PUT /exercises/{id} - Update exercise
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing exercise ID")
            )
            
            try {
                val exercise = call.receive<Exercise>()
                val exerciseWithId = exercise.copy(id = id)
                
                val updatedExercise = exerciseDataSource.update(exerciseWithId)
                if (updatedExercise != null) {
                    webSocketManager.emitExerciseUpdate(updatedExercise)
                    call.respond(HttpStatusCode.OK, updatedExercise)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Exercise not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // DELETE /exercises/{id} - Delete exercise
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing exercise ID")
            )
            
            try {
                val deleted = exerciseDataSource.delete(id)
                if (deleted) {
                    webSocketManager.emitExerciseUpdate(mapOf("deleted" to id))
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Exercise not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}