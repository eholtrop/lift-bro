package com.lift.bro.utils.logger

import platform.Foundation.NSLog


actual fun Log.d(tag: String?, message: String) {
    NSLog(message)
}