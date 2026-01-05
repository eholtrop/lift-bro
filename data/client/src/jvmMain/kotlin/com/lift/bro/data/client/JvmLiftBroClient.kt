package com.lift.bro.data.client

import io.ktor.client.engine.cio.*

/**
 * JVM-specific implementation of LiftBroClient using CIO engine
 */
actual fun createLiftBroClient(): LiftBroClient {
    val config = LiftBroClientConfig()

    val httpClient = createConfiguredHttpClient(
        platformEngine = CIO.create(),
        config = config
    )

    return KtorLiftBroClient(httpClient)
}
