package com.lift.bro.presentation.api.routes

import com.lift.bro.data.core.datasource.SetDataSource
import com.lift.bro.domain.models.LiftSet
import com.lift.bro.presentation.api.websocket.WebSocketManager
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.setRestRoutes(
    setDataSource: SetDataSource,
    webSocketManager: WebSocketManager
) {
    route("/sets") {
        
        // GET /sets?liftId=xxx - Get sets (optionally filtered by lift)
        get {
            val liftId = call.request.queryParameters["liftId"]
            try {
                val sets = if (liftId != null) {
                    setDataSource.getByLiftId(liftId)
                } else {
                    setDataSource.getAll()
                }
                call.respond(HttpStatusCode.OK, sets)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // GET /sets/{id} - Get specific set
        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respond(
                HttpStatusCode.BadRequest, 
                mapOf("error" to "Missing set ID")
            )
            
            try {
                val set = setDataSource.getById(id)
                if (set != null) {
                    call.respond(HttpStatusCode.OK, set)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Set not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
        
        // POST /sets - Create new set
        post {
            try {
                val set = call.receive<LiftSet>()
                val createdSet = setDataSource.create(set)
                
                // Emit update to WebSocket subscribers
                webSocketManager.emitSetUpdate(createdSet)
                
                call.respond(HttpStatusCode.Created, createdSet)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // PUT /sets/{id} - Update set
        put("/{id}") {
            val id = call.parameters["id"] ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing set ID")
            )
            
            try {
                val set = call.receive<LiftSet>()
                val setWithId = set.copy(id = id)
                
                val updatedSet = setDataSource.update(setWithId)
                if (updatedSet != null) {
                    webSocketManager.emitSetUpdate(updatedSet)
                    call.respond(HttpStatusCode.OK, updatedSet)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Set not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, mapOf("error" to e.message))
            }
        }
        
        // DELETE /sets/{id} - Delete set
        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Missing set ID")
            )
            
            try {
                val deleted = setDataSource.delete(id)
                if (deleted) {
                    webSocketManager.emitSetUpdate(mapOf("deleted" to id))
                    call.respond(HttpStatusCode.NoContent)
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("error" to "Set not found"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.message))
            }
        }
    }
}