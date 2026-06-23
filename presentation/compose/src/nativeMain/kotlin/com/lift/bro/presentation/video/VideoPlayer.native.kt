package com.lift.bro.presentation.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitView
import io.github.vinceglb.filekit.PlatformFile
import platform.AVFoundation.AVPlayer
import platform.AVKit.AVPlayerViewController
import platform.UIKit.UIView

@Composable
actual fun VideoPlayer(
    videoFile: PlatformFile,
    modifier: Modifier,
    onPlayPause: (() -> Unit)?,
) {
    val player = AVPlayer(uRL = videoFile.nsUrl)

    UIKitView(
        factory = {
            UIView().apply {
                this.addSubview(
                    AVPlayerViewController().apply {
                        this.player = player
                        this.showsPlaybackControls = true
                    }.view
                )
            }
        }
    )
}
