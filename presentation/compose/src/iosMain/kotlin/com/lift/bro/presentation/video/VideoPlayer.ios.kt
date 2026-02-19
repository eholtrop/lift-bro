package com.lift.bro.presentation.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
actual fun VideoPlayer(
    videoFile: java.io.File,
    modifier: Modifier,
    onPlayPause: (() -> Unit)?,
) {
    // Stub implementation - requires proper AVPlayer integration
    DisposableEffect(Unit) {
        onDispose { }
    }
}
