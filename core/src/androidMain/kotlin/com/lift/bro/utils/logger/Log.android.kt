package com.lift.bro.utils.logger

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}