package com.lift.bro.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp

/**
 * Android-specific implementation of LiftBroClient using OkHttp engine
 */
actual fun createLiftBroClient(config: LiftBroClientConfig): HttpClient {
    return createConfiguredHttpClient(
        platformEngine = OkHttp.create(),
        config = config
    )
}
