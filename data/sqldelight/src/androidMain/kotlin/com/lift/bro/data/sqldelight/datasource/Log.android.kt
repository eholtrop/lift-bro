package com.lift.bro.data.sqldelight.datasource

actual fun Log.d(tag: String?, message: String) {
    android.util.Log.d(tag, message)
}
