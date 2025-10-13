package com.lift.bro.domain.server

/**
 * Common server interface for all platforms
 */
interface LiftBroServer {
    fun isRunning(): Boolean
    fun start(port: Int = 8080, host: String = "0.0.0.0")
    fun stop()
}
