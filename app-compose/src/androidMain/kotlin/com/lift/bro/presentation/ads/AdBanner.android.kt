package com.lift.bro.presentation.ads

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.lift.bro.core.buildconfig.BuildKonfig

@Composable
actual fun AdBanner(modifier: Modifier) {

    var loadAd by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect("load_ads") {
        MobileAds.initialize(context) {
            loadAd = true
        }
    }

    if (loadAd) {
        AndroidView(
            modifier = modifier,
            factory = { context ->
                AdManagerAdView(context).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = BuildKonfig.ADMOB_AD_UNIT_ID
                    loadAd(AdManagerAdRequest.Builder().build())
                }
            }
        )
    }
}