package com.lift.bro.presentation.video

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
actual fun VideoPlayer(
    videoFile: PlatformFile,
    modifier: Modifier,
    onPlayPause: (() -> Unit)?,
) {
    // Stub implementation - requires proper AVPlayer integration
    DisposableEffect(Unit) {
        onDispose { }
    }
}
