package com.lift.bro.logger

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}