package com.lift.bro.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lift.bro.di.DependencyContainer
import com.lift.bro.presentation.App
import com.lift.bro.presentation.StoreManager

class MainActivity : ComponentActivity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DependencyContainer.context = this
        StoreManager.context = this
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        DependencyContainer.context = null
        StoreManager.context = null
    }
}
