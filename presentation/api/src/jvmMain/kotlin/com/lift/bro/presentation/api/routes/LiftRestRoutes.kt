package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.LiftDataSource
import com.lift.bro.domain.models.Lift
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.liftRestRoutes(
    liftDataSource: LiftDataSource,
    webSocketManager: WebSocketManager
) {
    route("/lifts") {
        
        // GET /lifts?workoutId=xxx - Get lifts (optionally filtered by workout)
        get {
            val workoutId = call.request.queryParameters["workoutId"]
            try {
                val lifts = if (workoutId != null) {
                    liftDataSource.getByWorkoutId(workoutId)
                } else {
                    liftDataSource.getAll()
                }
                call.respond(HttpStatusCode.OK, lifts)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // GET /lifts/{id} - Get specific lift
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Missing lift ID")
            )
            
            try {
                val lift = liftDataSource.getById(id)
                if (lift != null) {
                    call.respond(HttpStatusCode.OK, lift)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Lift not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // POST /lifts - Create new lift
        post {
            try {
                val lift = call.receive<Lift>()
                val createdLift = liftDataSource.create(lift)
                
                // Emit update to WebSocket subscribers
                webSocketManager.emitLiftUpdate(createdLift)
                
                call.respond(HttpStatusCode.Created, createdLift)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // PUT /lifts/{id} - Update lift
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing lift ID")
            )
            
            try {
                val lift = call.receive<Lift>()
                val liftWithId = lift.copy(id = id)
                
                val updatedLift = liftDataSource.update(liftWithId)
                if (updatedLift != null) {
                    webSocketManager.emitLiftUpdate(updatedLift)
                    call.respond(HttpStatusCode.OK, updatedLift)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Lift not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // DELETE /lifts/{id} - Delete lift
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing lift ID")
            )
            
            try {
                val deleted = liftDataSource.delete(id)
                if (deleted) {
                    webSocketManager.emitLiftUpdate(mapOf("deleted" to id))
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Lift not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}