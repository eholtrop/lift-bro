package com.lift.bro.presentation.server

import android.util.Log
import com.lift.bro.domain.server.LiftBroServer
import io.ktor.server.cio.*
import io.ktor.server.engine.*
import kotlinx.coroutines.*

class AndroidLiftBroServer : LiftBroServer {
    private var engine: ApplicationEngine? = null

    private var isRunning = false
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun isRunning(): Boolean = isRunning

    override fun start(port: Int, host: String) {
        if (isRunning) {
            Log.d("AndroidLiftBroServer", "Server already running")
            return
        }

        serverScope.launch {
            try {
                Log.d("AndroidLiftBroServer", "Creating embedded server...")
                val server = embeddedServer(CIO, port = port, host = host) {
                    configureLiftBroApp()
                    Log.d("AndroidLiftBroServer", "Lift Bro Server configured")
                }
                engine = server.engine
                server.start(wait = false)
                isRunning = true
                Log.d("AndroidLiftBroServer", "Server started on http://$host:$port")
            } catch (e: Exception) {
                Log.e("AndroidLiftBroServer", "Failed to start server", e)
                isRunning = false
            }
        }
    }

    override fun stop() {
        if (!isRunning) {
            Log.d("AndroidLiftBroServer", "Server already stopped")
            return
        }

        try {
            engine?.stop(1000, 5000)
            engine = null
            serverScope.cancel()
            isRunning = false
            Log.d("AndroidLiftBroServer", "Server stopped successfully")
        } catch (e: Exception) {
            Log.e("AndroidLiftBroServer", "Error stopping server", e)
            isRunning = false
        }
    }
}

actual fun createLiftBroServer(): LiftBroServer = AndroidLiftBroServer()
