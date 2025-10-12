package com.lift.bro.presentation.api.websocket

import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages WebSocket connections and streams data changes to connected clients
 */
class WebSocketManager {
    private val connections = ConcurrentHashMap<String, MutableSet<WebSocketSession>>()
    private val json = Json { 
        ignoreUnknownKeys = true 
        encodeDefaults = true
    }
    
    // Shared flows for each stream type
    private val _workoutUpdates = MutableSharedFlow<String>()
    private val _liftUpdates = MutableSharedFlow<String>() 
    private val _setUpdates = MutableSharedFlow<String>()
    private val _exerciseUpdates = MutableSharedFlow<String>()
    private val _settingsUpdates = MutableSharedFlow<String>()
    
    /**
     * Subscribe a WebSocket connection to a specific stream
     */
    fun subscribe(streamKey: String, session: WebSocketSession) {
        connections.computeIfAbsent(streamKey) { mutableSetOf() }.add(session)
        
        // Start streaming current data for this session
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when {
                    streamKey.startsWith("workouts") -> {
                        _workoutUpdates.asSharedFlow().collect { update ->
                            if (!session.isActive) return@collect
                            try {
                                session.send(Frame.Text(update))
                            } catch (e: Exception) {
                                // Connection closed, will be cleaned up
                            }
                        }
                    }
                    streamKey.startsWith("lifts") -> {
                        _liftUpdates.asSharedFlow().collect { update ->
                            if (!session.isActive) return@collect
                            try {
                                session.send(Frame.Text(update))
                            } catch (e: Exception) {
                                // Connection closed, will be cleaned up
                            }
                        }
                    }
                    streamKey.startsWith("sets") -> {
                        _setUpdates.asSharedFlow().collect { update ->
                            if (!session.isActive) return@collect
                            try {
                                session.send(Frame.Text(update))
                            } catch (e: Exception) {
                                // Connection closed, will be cleaned up
                            }
                        }
                    }
                    streamKey.startsWith("exercises") -> {
                        _exerciseUpdates.asSharedFlow().collect { update ->
                            if (!session.isActive) return@collect
                            try {
                                session.send(Frame.Text(update))
                            } catch (e: Exception) {
                                // Connection closed, will be cleaned up
                            }
                        }
                    }
                    streamKey.startsWith("settings") -> {
                        _settingsUpdates.asSharedFlow().collect { update ->
                            if (!session.isActive) return@collect
                            try {
                                session.send(Frame.Text(update))
                            } catch (e: Exception) {
                                // Connection closed, will be cleaned up
                            }
                        }
                    }
                }
            } finally {
                unsubscribe(streamKey, session)
            }
        }
    }
    
    /**
     * Unsubscribe a WebSocket connection from a stream
     */
    fun unsubscribe(streamKey: String, session: WebSocketSession) {
        connections[streamKey]?.remove(session)
        if (connections[streamKey]?.isEmpty() == true) {
            connections.remove(streamKey)
        }
    }
    
    /**
     * Emit updates to all subscribers of a stream
     */
    suspend fun emitWorkoutUpdate(data: Any) {
        val jsonData = json.encodeToString(data)
        _workoutUpdates.emit(jsonData)
    }
    
    suspend fun emitLiftUpdate(data: Any) {
        val jsonData = json.encodeToString(data)
        _liftUpdates.emit(jsonData)
    }
    
    suspend fun emitSetUpdate(data: Any) {
        val jsonData = json.encodeToString(data)
        _setUpdates.emit(jsonData)
    }
    
    suspend fun emitExerciseUpdate(data: Any) {
        val jsonData = json.encodeToString(data)
        _exerciseUpdates.emit(jsonData)
    }
    
    suspend fun emitSettingsUpdate(data: Any) {
        val jsonData = json.encodeToString(data)
        _settingsUpdates.emit(jsonData)
    }
    
    /**
     * Close all WebSocket connections
     */
    fun closeAllConnections() {
        connections.values.flatten().forEach { session ->
            try {
                if (session.isActive) {
                    runBlocking {
                        session.close(CloseReason(CloseReason.Codes.GOING_AWAY, "Server shutting down"))
                    }
                }
            } catch (e: Exception) {
                // Ignore exceptions during shutdown
            }
        }
        connections.clear()
    }
    
    /**
     * Get count of active connections for monitoring
     */
    fun getConnectionCount(): Int = connections.values.sumOf { it.size }
    
    fun getStreamKeys(): Set<String> = connections.keys.toSet()
}