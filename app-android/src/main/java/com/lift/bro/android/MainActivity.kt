package com.lift.bro.android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.core.content.ContextCompat
import com.lift.bro.di.DependencyContainer
import com.lift.bro.presentation.App
import com.lift.bro.presentation.LocalPlatformContext
import com.lift.bro.presentation.LocalServer
import com.lift.bro.presentation.StoreManager
import com.lift.bro.presentation.server.createLiftBroServer
import com.lift.bro.ui.navigation.Destination
import tv.dpal.navi.rememberNavCoordinator

class MainActivity : ComponentActivity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DependencyContainer.initialize(this)
        StoreManager.context = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val permissionsToRequest = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(Manifest.permission.READ_MEDIA_VIDEO)
            } else {
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { permissions ->
                val allPermissionsGranted = permissions.values.all { it }
                if (allPermissionsGranted) {
                    // All permissions granted
                } else {
                    // Handle permission denial
                }
            }

            LaunchedEffect(Unit) {
                val allPermissionsGranted = permissionsToRequest.all {
                    ContextCompat.checkSelfPermission(this@MainActivity, it) == PackageManager.PERMISSION_GRANTED
                }
                if (!allPermissionsGranted) {
                    launcher.launch(permissionsToRequest)
                }
            }

            val coordinator = rememberNavCoordinator(Destination.Unknown)
            CompositionLocalProvider(
                LocalPlatformContext provides LocalContext.current,
                LocalServer provides createLiftBroServer()
            ) {
                App(
                    modifier = Modifier.semantics {
                        // for ui tests. this ensures that our testTags will be readable by Appium
                        testTagsAsResourceId = true
                    },
                    navCoordinator = coordinator
                )
            }
            BackHandler {
                if (!coordinator.onBackPressed()) {
                    finish()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        StoreManager.context = null
    }
}
