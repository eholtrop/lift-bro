package com.lift.bro.presentation.video

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import java.io.File

expect @Composable
fun VideoPlayer(
    videoFile: File,
    modifier: Modifier = Modifier,
    onPlayPause: (() -> Unit)? = null,
)
