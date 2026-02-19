package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable

@Composable
actual fun rememberCameraPermissionHandler(): ICameraPermissionHandler {
    return remember {
        object : ICameraPermissionHandler {
            override val isGranted: Boolean = true

            override fun requestPermission(onResult: (Boolean) -> Unit) {
                onResult(true)
            }
        }
    }
}
