package com.lift.bro.di

import android.content.Context
import android.content.Intent
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.presentation.Coordinator

actual class DependencyContainer {

    companion object {
        var context: Context? = null
    }

    actual val database: LBDatabase
        get() = LBDatabase(DriverFactory(context!!))

    actual fun launchCalculator() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_CALCULATOR)
        context?.startActivity(intent)
    }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }