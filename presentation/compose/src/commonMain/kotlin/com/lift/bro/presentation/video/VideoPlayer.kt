package com.lift.bro.presentation.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.vinceglb.filekit.PlatformFile

@Composable
expect fun VideoPlayer(
    videoFile: PlatformFile,
    modifier: Modifier = Modifier,
    onPlayPause: (() -> Unit)? = null,
)
