package com.lift.bro.data.sqldelight.datasource

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
