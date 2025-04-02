package com.lift.bro.domain.repositories

import com.lift.bro.data.Backup
import com.lift.bro.domain.models.Settings
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ISettingsRepository {

    fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight>

    fun saveUnitOfMeasure(uom: Settings.UnitOfWeight)

    fun getBackupSettings(): Flow<BackupSettings>

    fun saveBackupSettings(settings: BackupSettings)
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)