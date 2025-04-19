package com.lift.bro.logger

import com.lift.bro.utils.logger.Log

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}