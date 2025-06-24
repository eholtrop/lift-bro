package com.lift.bro.di

import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.UserDefaultsSettingsRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import platform.Foundation.NSDictionary
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class DependencyContainer {

    actual val database: LBDatabase by lazy { LBDatabase(DriverFactory()) }

    actual val settingsRepository: ISettingsRepository by lazy { UserDefaultsSettingsRepository() }

    actual fun launchCalculator() {

    }

    actual fun launchUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }

    actual fun launchManageSubscriptions() {
    }
}

actual val dependencies: DependencyContainer by lazy {
    DependencyContainer()
}