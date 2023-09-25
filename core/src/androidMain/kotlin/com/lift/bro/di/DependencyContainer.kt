package com.lift.bro.di

import android.content.Context
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.presentation.Coordinator
import com.lift.bro.presentation.voyager.VoyagerCoordinator

actual class DependencyContainer {

    companion object {
        var context: Context? = null
    }

    actual val database: LBDatabase
        get() = LBDatabase(DriverFactory(context!!))

    actual val coordinator: Coordinator by lazy { VoyagerCoordinator() }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }