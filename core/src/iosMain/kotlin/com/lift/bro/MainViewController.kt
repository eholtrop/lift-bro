package com.lift.bro

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.lift.bro.presentation.App
import com.lift.bro.presentation.LocalAdBannerProvider
import platform.UIKit.UIView

fun MainViewController(
    bannerProvider: () -> UIView
) = ComposeUIViewController {
    CompositionLocalProvider(
        LocalAdBannerProvider provides bannerProvider
    ) {
        App()
    }
}