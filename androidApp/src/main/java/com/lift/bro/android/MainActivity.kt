package com.lift.bro.android

import App
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.lift.bro.di.DependencyContainer

class MainActivity : ComponentActivity() {

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        DependencyContainer.context = this
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
    }
}
