package com.lift.bro.presentation.settings.server

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.net.Inet4Address

@Composable
actual fun getWifiIpAddress(): String? {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork
    val capabilities = connectivityManager.getNetworkCapabilities(network)
    val isWifi = capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true
    val linkProperties = if (isWifi) connectivityManager.getLinkProperties(network) else null
    val wifiAddress = linkProperties?.linkAddresses
        ?.map { it.address }
        ?.filterIsInstance<Inet4Address>()
        ?.firstOrNull()
        ?.hostAddress
    return if (isWifi) wifiAddress else null
}
