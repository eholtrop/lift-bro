package com.lift.bro.utils.logger

import com.lift.bro.config.BuildConfig

actual fun Log.d(tag: String?, message: String) {
    if (BuildConfig.isDebug) {
        android.util.Log.d(tag, message)
    }
}