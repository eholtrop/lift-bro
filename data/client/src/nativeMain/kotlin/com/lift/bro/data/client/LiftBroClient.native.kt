package com.lift.bro.data.client

import io.ktor.client.HttpClient
import io.ktor.client.engine.darwin.Darwin

actual fun createLiftBroClient(config: LiftBroClientConfig): HttpClient {
    return createConfiguredHttpClient(
        platformEngine = Darwin.create {
            configureRequest {
                setAllowsCellularAccess(true)
            }
        },
        config = config
    )
}
