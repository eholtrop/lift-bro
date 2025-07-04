package com.lift.bro.domain.repositories

import com.example.compose.ThemeMode
import com.lift.bro.domain.models.MERSettings
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

    fun getMerSettings(): Flow<MERSettings>

    fun setMerSettings(merSettings: MERSettings)

    fun getLatestReadReleaseNotes(): Flow<String?>

    fun setLatestReadReleaseNotes(versionId: String)

    fun getThemeMode(): Flow<ThemeMode>

    fun setThemeMode(themeMode: ThemeMode)
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)