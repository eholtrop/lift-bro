package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable

@Composable
expect fun rememberCameraPermissionHandler(): ICameraPermissionHandler

interface ICameraPermissionHandler {
    val isGranted: Boolean
    fun requestPermission(onResult: (Boolean) -> Unit)
}
