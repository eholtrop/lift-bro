package com.lift.bro.di

import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.repositories.ISettingsRepository

expect class DependencyContainer {
    val database: LBDatabase

    val settingsRepository: ISettingsRepository

    fun launchCalculator()
}

expect val dependencies: DependencyContainer