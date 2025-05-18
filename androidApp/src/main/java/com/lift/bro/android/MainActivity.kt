package com.lift.bro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
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
            val coordinator = rememberNavCoordinator(Destination.Dashboard)
            App(
                navCoordinator = coordinator
            )

            BackHandler {
                if (!coordinator.onBackPressed()) {
                    onBackPressed()
                }
            }
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        StoreManager.context = null
    }
}
