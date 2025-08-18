package com.lift.bro.domain.repositories

import com.example.compose.ThemeMode
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.presentation.onboarding.LiftBro
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Consent(
    val deviceId: String,
    val consentDateTime: LocalDateTime,
    val tncVersion: Double,
    val privacyPolicyVersion: Double,
    val appVersion: String
)

interface ISettingsRepository {

    fun getDeviceId(): String

    fun getDeviceConsent(): Flow<Consent?>

    fun setDeviceConsent(consent: Consent)

    fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight>

    fun saveUnitOfMeasure(uom: Settings.UnitOfWeight)

    fun getDeviceFtux(): Flow<Boolean>

    fun setDeviceFtux(ftux: Boolean)

    fun getBackupSettings(): Flow<BackupSettings>

    fun saveBackupSettings(settings: BackupSettings)

    fun getBro(): Flow<LiftBro?>

    fun setBro(bro: LiftBro)

    fun getMerSettings(): Flow<MERSettings>

    fun setMerSettings(merSettings: MERSettings)

    fun showTotalWeightMoved(show: Boolean)

    fun shouldShowTotalWeightMoved(): Flow<Boolean>

    fun getLatestReadReleaseNotes(): Flow<String?>

    fun setLatestReadReleaseNotes(versionId: String)

    fun getThemeMode(): Flow<ThemeMode>

    fun setThemeMode(themeMode: ThemeMode)

    fun eMaxEnabled(): Flow<Boolean>

    fun setEMaxEnabled(enabled: Boolean)

    fun tMaxEnabled(): Flow<Boolean>

    fun setTMaxEnabled(enabled: Boolean)
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)