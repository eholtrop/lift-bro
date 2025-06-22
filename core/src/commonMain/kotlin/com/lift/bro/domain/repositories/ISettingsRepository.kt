package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.Settings
import com.lift.bro.presentation.onboarding.LiftBro
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ISettingsRepository {

    fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight>

    fun getDeviceFtux(): Flow<Boolean>

    fun setDeviceFtux(ftux: Boolean)

    fun saveUnitOfMeasure(uom: Settings.UnitOfWeight)

    fun getBackupSettings(): Flow<BackupSettings>

    fun saveBackupSettings(settings: BackupSettings)

    fun getBro(): Flow<LiftBro?>

    fun setBro(bro: LiftBro)

    fun shouldShowMerCalcs(): Flow<Boolean>

    fun setShowMerCalcs(showMerCalcs: Boolean)

    fun getLatestReadReleaseNotes(): Flow<String?>

    fun setLatestReadReleaseNotes(versionId: String)
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)