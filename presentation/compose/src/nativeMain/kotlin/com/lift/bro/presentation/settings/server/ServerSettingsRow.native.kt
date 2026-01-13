package com.lift.bro.presentation.settings.server

actual fun getLocalIPAdderess(): String? {
    // iOS doesn't have an easy way to get local IP without additional framework
    // This would require using ifaddrs from posix which is more complex
    // For now, return null and the UI will handle it
    return null
}
