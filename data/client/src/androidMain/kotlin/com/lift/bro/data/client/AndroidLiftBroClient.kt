package com.lift.bro.data.client

import io.ktor.client.engine.okhttp.*

/**
 * Android-specific implementation of LiftBroClient using OkHttp engine
 */
actual fun createLiftBroClient(): LiftBroClient {
    val config = LiftBroClientConfig()
    
    val httpClient = createConfiguredHttpClient(
        platformEngine = OkHttp.create(),
        config = config
    )
    
    return KtorLiftBroClient(httpClient)
}