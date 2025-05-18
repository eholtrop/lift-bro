package com.lift.bro.di

import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.utils.today
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.Clock

actual class DependencyContainer {

    actual val database: LBDatabase by lazy { LBDatabase(DriverFactory()) }

    actual val settingsRepository: ISettingsRepository
        get() = object : ISettingsRepository {
            override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
                return flowOf(Settings.UnitOfWeight(UOM.POUNDS))
            }

            override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {

            }

            override fun getBackupSettings(): Flow<BackupSettings> {
                return flowOf(BackupSettings(
                    lastBackupDate = Clock.System.today
                ))
            }

            override fun saveBackupSettings(settings: BackupSettings) {
            }
        }

    actual fun launchCalculator() {

    }
}

actual val dependencies: DependencyContainer by lazy {
    DependencyContainer()
}