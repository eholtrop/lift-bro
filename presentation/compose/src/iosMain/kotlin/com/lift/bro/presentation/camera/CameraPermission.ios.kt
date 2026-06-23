package com.lift.bro.presentation.camera

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import platform.AVFoundation.AVAuthorizationStatusAuthorized
import platform.AVFoundation.AVCaptureDevice
import platform.AVFoundation.AVMediaTypeVideo
import platform.AVFoundation.authorizationStatusForMediaType
import platform.AVFoundation.requestAccessForMediaType

@Composable
actual fun rememberCameraPermissionHandler(): ICameraPermissionHandler {
    var hasPermission by remember { mutableStateOf(checkCameraPermission()) }
    val scope = rememberCoroutineScope()

    return remember {
        object : ICameraPermissionHandler {
            override val isGranted: Boolean get() = hasPermission

            override fun requestPermission(onResult: (Boolean) -> Unit) {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    scope.launch(Dispatchers.Main) {
                        hasPermission = granted
                        onResult(granted)
                    }
                }
            }
        }
    }
}

private fun checkCameraPermission(): Boolean {
    return AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo) == AVAuthorizationStatusAuthorized
}
