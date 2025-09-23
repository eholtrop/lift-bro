package com.lift.bro.presentation.ads

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import com.lift.bro.presentation.LocalAdBannerProvider
import platform.UIKit.UIView

@Composable
actual fun AdBanner(modifier: Modifier) {
    val view = LocalAdBannerProvider.current
    UIKitView(
        factory = {
            view.invoke() as UIView
        },
        modifier = modifier,
    )
}