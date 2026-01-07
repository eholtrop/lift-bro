package com.lift.bro.presentation.server

import android.util.Log
import com.lift.bro.domain.server.LiftBroServer
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class AndroidLiftBroServer : LiftBroServer {
    private var engine: ApplicationEngine? = null
    private val serverScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    var serverJob: Job? = null

    override fun isRunning(): Boolean = serverJob?.isActive == true

    override fun start(port: Int, host: String) {
        if (serverJob?.isActive == true) {
            Log.d("AndroidLiftBroServer", "Server already running")
            return
        }

        serverJob = serverScope.launch {
            try {
                Log.d("AndroidLiftBroServer", "Creating embedded server...")
                val server = embeddedServer(CIO, port = port, host = host) {
                    configureLiftBroApp()
                    Log.d("AndroidLiftBroServer", "Lift Bro Server configured")
                }
                engine = server.engine
                server.start(wait = false)
                Log.d("AndroidLiftBroServer", "Server started on http://$host:$port")
            } catch (e: Exception) {
                serverJob?.cancel(cause = CancellationException("Failed to start server", e))
            }
        }
    }

    override fun stop() {
        if (serverJob?.isActive == false) {
            Log.d("AndroidLiftBroServer", "Server already stopped")
            return
        }

        try {
            engine?.stop(1000, 5000)
            engine = null
            serverJob?.cancel()
            serverJob = null
            Log.d("AndroidLiftBroServer", "Server stopped successfully")
        } catch (e: Exception) {
            Log.e("AndroidLiftBroServer", "Error stopping server", e)
            serverJob?.cancel()
            serverJob= null
        }
    }
}

actual fun createLiftBroServer(): LiftBroServer = AndroidLiftBroServer()
