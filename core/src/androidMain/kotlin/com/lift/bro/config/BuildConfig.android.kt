package com.lift.bro.config

import com.lift.bro.BuildConfig

actual object BuildConfig {
    actual val isDebug: Boolean
        get() = BuildConfig.DEBUG
}