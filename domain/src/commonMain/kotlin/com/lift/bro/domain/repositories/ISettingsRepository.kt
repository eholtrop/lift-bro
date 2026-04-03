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
    data object Consent: Setting<com.lift.bro.domain.repositories.Consent?>
    data object UnitOfMeasure: Setting<Settings.UnitOfWeight>
    data object DeviceFtux: Setting<Boolean>
    data object BackupSettings: Setting<com.lift.bro.domain.repositories.BackupSettings>
    data object Bro: Setting<LiftBro>
    data object MerSettings: Setting<MERSettings>
    data object ShowTotalWeightMoved: Setting<Boolean>
    data object LatestReadReleaseNotes: Setting<String?>
    data object ThemeMode: Setting<com.lift.bro.domain.models.ThemeMode>
    data object EMaxEnabled: Setting<Boolean>
    data object TMaxEnabled: Setting<Boolean>
    data object ClientUrl: Setting<String?>
}

interface ISettingsRepository {

    fun <T> listen(setting: Setting<T>): Flow<T>
    suspend fun <T> get(setting: Setting<T>): T
    fun <T> set(setting: Setting<T>, value: T)

    fun getDeviceId(): String

    fun getClientUrl(): String?
}

data class BackupSettings(
    val lastBackupDate: LocalDate,
)
