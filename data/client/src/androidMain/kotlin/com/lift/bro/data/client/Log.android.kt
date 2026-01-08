package com.lift.bro.data.client

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}
