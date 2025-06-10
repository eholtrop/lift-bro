package com.lift.bro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.google.android.gms.ads.MobileAds
import com.lift.bro.BuildConfig
import com.lift.bro.di.DependencyContainer
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.App
import com.lift.bro.presentation.StoreManager
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.rememberNavCoordinator
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DependencyContainer.initialize(this)
        StoreManager.context = this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            LaunchedEffect("admob_initialization") {
                if (BuildConfig.DEBUG) {
                    MobileAds.initialize(this@MainActivity)
                }
            }

            val coordinator = rememberNavCoordinator(Destination.Unknown)
            App(navCoordinator = coordinator)
            BackHandler {
                if (!coordinator.onBackPressed()) {
                    onBackPressed()
                }
            }

            LaunchedEffect("landing_selection") {
                dependencies.settingsRepository.getDeviceFtux().collectLatest {
                    when (it) {
                        true -> coordinator.setRoot(Destination.Dashboard)
                        false -> coordinator.setRoot(Destination.Onboarding)
                    }
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        StoreManager.context = null
    }
}
