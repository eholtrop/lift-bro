package com.lift.bro.presentation.server

import io.ktor.server.cio.*
import io.ktor.server.engine.*

class JvmLiftBroServer : LiftBroServer {
    private var server: ApplicationEngine? = null

    override fun start(port: Int, host: String) {
        server = embeddedServer(CIO, port = port, host = host) {
            configureLiftBroApp()
        }.engine
        server?.start(wait = false)
    }

    override fun stop() {
        server?.stop(1000, 5000)
        server = null
    }
}

actual fun createLiftBroServer(): LiftBroServer = JvmLiftBroServer()

// Entry point for standalone JVM applications
fun main() {
    val server = createLiftBroServer()
    server.start()

    // Keep the application running
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop()
    })

    // Wait for termination
    Thread.currentThread().join()
}
