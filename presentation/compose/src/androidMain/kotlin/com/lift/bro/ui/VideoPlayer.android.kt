package com.lift.bro.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import java.io.File

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(
    modifier: Modifier,
    uri: String,
) {
    val context = LocalContext.current

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = remember {
        context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
    }

    var showVideo by remember { mutableStateOf(false) }
// Use a Permission Launcher to request it
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showVideo = true
        }
    }

    LaunchedEffect(Unit) {
        if (context.checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            launcher.launch(permission)
        } else {
            showVideo = true
        }
    }

    if (showVideo) {

        // 1. Create a physical file in your app's private cache
        val internalFile = remember(uri) {
            try {
                val contentUri = Uri.parse(uri)
                // Create a unique temp file in /data/user/0/com.lift.bro/cache/
                val tempFile = File(context.cacheDir, "preview_${System.currentTimeMillis()}.mp4")

                context.contentResolver.openInputStream(contentUri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            } catch (e: Exception) {
                null
            }
        }

        val player = remember(internalFile) {
            val exoPlayer = ExoPlayer.Builder(context).build()

            if (internalFile != null && internalFile.exists()) {
                // Use the ABSOLUTE PATH of the local file (No content:// issues here)
                exoPlayer.setMediaItem(MediaItem.fromUri(Uri.fromFile(internalFile)))
            } else {
                // Last ditch effort if copy failed
                exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            }

            exoPlayer.prepare()
            exoPlayer.playWhenReady = true
            exoPlayer
        }

        DisposableEffect(player) {
            onDispose {
                player.release()
                // Cleanup: Delete the temp file when the composable leaves the screen
                internalFile?.delete()
            }
        }

        AndroidView(
            modifier = modifier,
            factory = { ctx ->
                PlayerView(ctx).apply {
                    this.player = player
                    this.useController = true
                }
            }
        )
    }
}
