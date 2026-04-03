package com.lift.bro.domain.repositories

import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.ThemeMode
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
    val appVersion: String,
)

sealed interface Setting<T> {
    data object Timer: Setting<Boolean>
    data object DashboardV3: Setting<Boolean>
    data object EditSetVersion: Setting<Int>
    data object Consent: Setting<Consent?>
    data object UnitOfMeasure: Setting<Settings.UnitOfWeight>
    data object DeviceFtux: Setting<Boolean>
    data object BackupSettings: Setting<BackupSettings>
    data object Bro: Setting<LiftBro>
    data object MerSettings: Setting<MERSettings>
    data object ShowTotalWeightMoved: Setting<Boolean>
    data object LatestReadReleaseNotes: Setting<String?>
    data object ThemeMode: Setting<ThemeMode>
    data object EMaxEnabled: Setting<Boolean>
    data object TMaxEnabled: Setting<Boolean>
    data object ClientUrl: Setting<String?>
}

interface ISettingsRepository {

    fun <T> listen(setting: Setting<T>): Flow<T>
    suspend fun <T> get(setting: Setting<T>): T
    fun <T> set(setting: Setting<T>, value: T)

    fun enableTimer(): Boolean

    fun setEnableTimer(enabled: Boolean)

    fun dashboardV3(): Boolean

    fun enableDashboardV3(enabled: Boolean)

    fun editSetVersion(): Int

    fun setEditSetVersion(version: Int)

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

    fun getClientUrl(): String?

    fun setClientUrl(url: String?)
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)
