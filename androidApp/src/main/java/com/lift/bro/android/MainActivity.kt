package com.lift.bro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import com.google.android.gms.ads.MobileAds
import com.lift.bro.BuildConfig
import com.lift.bro.di.DependencyContainer
import com.lift.bro.presentation.App
import com.lift.bro.presentation.StoreManager
import com.lift.bro.ui.navigation.Destination
import com.lift.bro.ui.navigation.rememberNavCoordinator

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
            App(
                modifier = Modifier.semantics {
                    // for ui tests. this ensures that our testTags will be readable by Appium
                    testTagsAsResourceId = true
                },
                navCoordinator = coordinator
            )
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
