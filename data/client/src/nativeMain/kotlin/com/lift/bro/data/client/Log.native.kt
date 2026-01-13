package com.lift.bro.data.client

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
