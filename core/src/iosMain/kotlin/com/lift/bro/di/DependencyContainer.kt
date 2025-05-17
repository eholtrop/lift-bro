package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.repositories.ISettingsRepository

actual class DependencyContainer {

    actual val database: LBDatabase
        get() = TODO("Not yet implemented")

    actual val settingsRepository: ISettingsRepository
        get() = TODO("Not yet implemented")

    actual fun launchCalculator() {

    }
}