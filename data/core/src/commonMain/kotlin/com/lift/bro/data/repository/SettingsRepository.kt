package com.lift.bro.data.repository

import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.UserPreferencesDataSource
import com.lift.bro.domain.models.BackupSettings
import com.lift.bro.domain.models.Consent
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Setting
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.ISettingsRepository
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

@Suppress("TooManyFunctions")
class SettingsRepository(
    private val dataSource: UserPreferencesDataSource,
): ISettingsRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val refreshKey by lazy { MutableSharedFlow<String>() }

    private fun keyChanged(key: String) {
        scope.launch {
            refreshKey.emit(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(setting: Setting<T>): T {
        val key = keyForSetting(setting)
        return when (setting) {
            Setting.TimerEnabled -> dataSource.getBool(key, false)
            Setting.DashboardV3 -> dataSource.getBool(key, false)
            Setting.EditSetVersion -> dataSource.getInt(key, 1)
            Setting.DeviceId -> getOrCreateDeviceId()
            Setting.DeviceConsent -> dataSource.getSerializable<Consent>(key, null)
            Setting.UnitOfMeasure -> Settings.UnitOfWeight(UOM.valueOf(dataSource.getString(key) ?: "POUNDS"))
            Setting.DeviceFtux -> dataSource.getBool(key, false)
            Setting.BackupSettingsKey -> BackupSettings(LocalDate.fromEpochDays(dataSource.getInt(key, 0)))
            Setting.Bro -> dataSource.getString(key)?.let { LiftBro.valueOf(it) }
            Setting.MerSettings -> dataSource.getSerializable<MERSettings>(key, null)
                ?: MERSettings(enabled = isUserProDefault())

            Setting.LatestReadReleaseNotes -> dataSource.getString(key)
            Setting.ThemeModeKey -> dataSource.getString(key)?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            Setting.EMaxEnabled -> dataSource.getBool(key, isUserProDefault())
            Setting.TMaxEnabled -> dataSource.getBool(key, isUserProDefault())
            Setting.ClientUrl -> dataSource.getString(key, null)
            Setting.ShowTotalWeightMoved -> dataSource.getBool(key, isUserProDefault())
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> set(setting: Setting<T>, value: T) {
        val key = keyForSetting(setting)
        when (value) {
            is Int -> dataSource.getInt(key, value)
            is String -> dataSource.putString(key, value)
            is String? -> dataSource.putString(key, value)
            is Boolean -> dataSource.putBool(key, value)
            is Consent? -> dataSource.putSerializable(key, value as Consent?)
            is Settings.UnitOfWeight -> dataSource.putString(key, (value).uom.toString())
            is BackupSettings -> dataSource.putInt(key, (value as BackupSettings).lastBackupDate.toEpochDays().toInt())
            is ThemeMode -> dataSource.putString(key, (value as ThemeMode).toString())
            is LiftBro -> dataSource.putString(key, value.name)
            is MERSettings -> dataSource.putSerializable(key, value as MERSettings)
            else -> error("Type of ${value::class.simpleName} is not recognized")
        }
        keyChanged(key)
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> listen(setting: Setting<T>): Flow<T> {
        val key = keyForSetting(setting)
        return refreshKey
            .filter { it == key }
            .map { get(setting) }
            .onStart {
                emit(get(setting))
            }
    }

    private fun <T> keyForSetting(setting: Setting<T>): String {
        return when (setting) {
            Setting.TimerEnabled -> "timer_feature_flag"
            Setting.EditSetVersion -> "edit_set_screen_version"
            Setting.DashboardV3 -> "dashboard_v3"
            Setting.DeviceId -> "device_id"
            Setting.DeviceConsent -> "consent"
            Setting.UnitOfMeasure -> "unit_of_measure"
            Setting.DeviceFtux -> "ftux"
            Setting.BackupSettingsKey -> "last_backup_epoch_days"
            Setting.Bro -> "bro"
            Setting.MerSettings -> "mer_settings"
            Setting.LatestReadReleaseNotes -> "latest_read_release_notes"
            Setting.ThemeModeKey -> "theme_mode"
            Setting.EMaxEnabled -> "emax_enabled"
            Setting.TMaxEnabled -> "tmax_enabled"
            Setting.ClientUrl -> "remote_client_url"
            Setting.ShowTotalWeightMoved -> "show_twm"
        }
    }

    private fun getOrCreateDeviceId(): String {
        return dataSource.getString("device_id") ?: uuid4().toString().also {
            dataSource.putString("device_id", it)
        }
    }

    private fun isUserProDefault(): Boolean {
        return try {
            runBlocking {
                Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.contains("pro")
            }
        } catch (e: Exception) {
            return false
        }
    }
}
