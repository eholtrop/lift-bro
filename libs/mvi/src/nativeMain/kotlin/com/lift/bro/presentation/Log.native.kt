package com.lift.bro.presentation

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
