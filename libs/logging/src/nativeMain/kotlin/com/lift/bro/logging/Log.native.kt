package com.lift.bro.logging

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
