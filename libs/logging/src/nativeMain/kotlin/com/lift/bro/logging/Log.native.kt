package com.lift.bro.logging

import com.lift.bro.logging.Log

actual fun Log.d(tag: String?, message: String) {
    println("[$tag] $message")
}
