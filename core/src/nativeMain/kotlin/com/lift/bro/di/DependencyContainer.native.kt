package com.lift.bro.di

import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.UserDefaultsSettingsRepository
import com.lift.bro.domain.repositories.ISettingsRepository

actual class DependencyContainer {

    actual val database: LBDatabase by lazy { LBDatabase(DriverFactory()) }

    actual val settingsRepository: ISettingsRepository by lazy { UserDefaultsSettingsRepository() }

    actual fun launchCalculator() {

    }

    actual fun launchUrl(url: String) {
    }
}

actual val dependencies: DependencyContainer by lazy {
    DependencyContainer()
}