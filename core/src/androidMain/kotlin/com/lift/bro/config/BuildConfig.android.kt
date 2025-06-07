package com.lift.bro.config

import com.lift.bro.core.BuildConfig

actual object BuildConfig {
    actual val isDebug: Boolean
        get() = BuildConfig.DEBUG
}