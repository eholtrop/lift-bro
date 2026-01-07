package com.lift.bro.presentation

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}
