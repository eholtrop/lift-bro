package com.lift.bro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
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
