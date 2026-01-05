package com.lift.bro.utils.logger

import com.lift.bro.config.BuildConfig
import platform.Foundation.NSLog

actual fun Log.d(tag: String?, message: String) {
    if (BuildConfig.isDebug) {
        NSLog("$tag: $message")
    }
}
