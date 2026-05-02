package com.lift.bro.data.repository

import com.benasher44.uuid.uuid4
import com.lift.bro.AppPurchases
import com.lift.bro.data.datasource.UserPreferencesDataSource
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.models.settings.AnalyticsConsent
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

class SettingsRepository(
    private val dataSource: UserPreferencesDataSource,
): ISettingsRepository {

    private val refreshKey by lazy { MutableSharedFlow<String>() }

    private fun keyChanged(key: String) {
        GlobalScope.launch {
            refreshKey.emit(key)
        }
    }

    private fun <R> subscribeToKey(key: String, block: suspend (String) -> R): Flow<R> {
        return refreshKey
            .filter { it == key }
            .map(block)
            .onStart {
                emit(block(key))
            }
    }

    override fun <T> listen(setting: Setting<T>): Flow<T> {
        return subscribeToKey(setting.key) { setting.value() }
    }

    override suspend fun <T> get(setting: Setting<T>): T {
        return setting.value()
    }

    override fun <T> set(setting: Setting<T>, value: T) {
        val key = setting.key
        when (setting) {
            Setting.BackupSettings -> dataSource.putInt(
                key,
                (value as BackupSettings).lastBackupDate.toEpochDays().toInt()
            )

            Setting.Bro -> dataSource.putString(key, (value as LiftBro).name)
            Setting.ClientUrl -> dataSource.putString(key, value as String?)
            Setting.Consent -> dataSource.putSerializable(key, value as Consent?)
            Setting.DashboardV3 -> dataSource.putBool(key, value as Boolean)
            Setting.DeviceFtux -> dataSource.putBool(key, value as Boolean)
            Setting.EMaxEnabled -> dataSource.putBool(key, value as Boolean)
            Setting.EditSetVersion -> dataSource.putInt(key, value as Int)
            Setting.LatestReadReleaseNotes -> dataSource.putString(key, value as String?)
            Setting.MerSettings -> dataSource.putSerializable(key, value as MERSettings)
            Setting.ShowTotalWeightMoved -> dataSource.putBool(key, value as Boolean)
            Setting.TMaxEnabled -> dataSource.putBool(key, value as Boolean)
            Setting.ThemeMode -> dataSource.putString(key, (value as ThemeMode).toString())
            Setting.Timer -> dataSource.putBool(key, value as Boolean)
            Setting.AnalyticsConsent -> dataSource.putSerializable(key, value as AnalyticsConsent)
            Setting.UnitOfMeasure -> dataSource.putString(key, (value as Settings.UnitOfWeight).uom.toString())
        }
        keyChanged(key)
    }

    private val Setting<*>.key: String
        get() = when (this) {
            Setting.BackupSettings -> "last_backup_epoch_days"
            Setting.Bro -> "bro"
            Setting.ClientUrl -> "remote_client_url"
            Setting.Consent -> "consent"
            Setting.DashboardV3 -> "dashboard_v3"
            Setting.DeviceFtux -> "ftux"
            Setting.EMaxEnabled -> "emax_enabled"
            Setting.EditSetVersion -> "edit_set_screen_version"
            Setting.LatestReadReleaseNotes -> "latest_read_release_notes"
            Setting.MerSettings -> "mer_settings"
            Setting.ShowTotalWeightMoved -> "show_twm"
            Setting.TMaxEnabled -> "tmax_enabled"
            Setting.ThemeMode -> "theme_mode"
            Setting.Timer -> "timer_feature_flag"
            Setting.UnitOfMeasure -> "unit_of_measure"
            Setting.AnalyticsConsent -> "analytics_consent"
        }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> Setting<T>.value(): T {
        return when (this) {
            Setting.BackupSettings -> BackupSettings(
                lastBackupDate = LocalDate.fromEpochDays(dataSource.getInt(key, 0))
            ) as T

            Setting.Bro -> dataSource.getString(key, null)?.let { LiftBro.valueOf(it) }
                ?: LiftBro.entries.toTypedArray().random()

            Setting.ClientUrl -> dataSource.getString(key, null)
            Setting.Consent -> dataSource.getSerializable<Consent>(key, null)
            Setting.DashboardV3 -> dataSource.getBool(key, false)
            Setting.DeviceFtux -> dataSource.getBool(key, false)
            Setting.EMaxEnabled -> dataSource.getBool(key, AppPurchases.isUserPro())
            Setting.EditSetVersion -> dataSource.getInt(key, 1)
            Setting.AnalyticsConsent -> dataSource.getSerializable<AnalyticsConsent>(
                key,
                AnalyticsConsent(
                    dashboardBannerDismissed = false,
                    consented = false
                )
            )
            Setting.LatestReadReleaseNotes -> dataSource.getString(key, null)
            Setting.MerSettings -> dataSource.getSerializable<MERSettings>(key, null)
                ?: MERSettings(enabled = AppPurchases.isUserPro())

            Setting.ShowTotalWeightMoved -> dataSource.getBool(key, AppPurchases.isUserPro())
            Setting.TMaxEnabled -> dataSource.getBool(key, AppPurchases.isUserPro())
            Setting.ThemeMode -> dataSource.getString(key, null)?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            Setting.Timer -> dataSource.getBool(key, false)
            Setting.UnitOfMeasure -> Settings.UnitOfWeight(UOM.valueOf(dataSource.getString(key, "POUNDS") ?: "POUNDS"))
        } as T
    }

    override fun getDeviceId(): String {
        return dataSource.getString("device_id") ?: uuid4().toString().also {
            dataSource.putString("device_id", it)
        }
    }

    override fun getClientUrl(): String? {
        return dataSource.getString("remote_client_url", null)
    }
}
