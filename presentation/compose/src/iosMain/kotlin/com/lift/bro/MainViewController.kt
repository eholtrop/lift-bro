package com.lift.bro

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.window.ComposeUIViewController
import com.lift.bro.presentation.App
import com.lift.bro.presentation.LocalAdBannerProvider
import com.lift.bro.presentation.LocalPlatformContext
import com.lift.bro.presentation.Platform
import platform.UIKit.UIView

fun MainViewController(
    bannerProvider: () -> UIView
) = ComposeUIViewController {
    CompositionLocalProvider(
        LocalAdBannerProvider provides bannerProvider
    ) {
        val keyboard = LocalSoftwareKeyboardController.current
        CompositionLocalProvider(
            LocalPlatformContext provides Platform.iOS
        ) {
            App(
                modifier = Modifier.pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            keyboard?.hide()
                        },
                    )
                }
            )
        }
    }
}
