package com.lift.bro.presentation.settings.server

import java.net.NetworkInterface

actual fun getLocalIPAdderess(): String? {
    val interfaces = NetworkInterface.getNetworkInterfaces()
    while (interfaces.hasMoreElements()) {
        val networkInterface = interfaces.nextElement()

        if (!networkInterface.isLoopback && networkInterface.isUp) {
            val address = networkInterface.inetAddresses
            while (address.hasMoreElements()) {
                val nextElement = address.nextElement()
                if (nextElement.isSiteLocalAddress && nextElement.hostAddress?.contains(".") == true) {
                    return nextElement.hostAddress
                }
            }
        }
    }
    return null
}
