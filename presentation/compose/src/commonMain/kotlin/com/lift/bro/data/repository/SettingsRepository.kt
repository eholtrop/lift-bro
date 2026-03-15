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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDate

class SettingsRepository(
    private val dataSource: UserPreferencesDataSource,
) : ISettingsRepository {

    private val scope = CoroutineScope(Dispatchers.Default)
    private val refreshKey by lazy { MutableSharedFlow<String>() }

    private fun keyChanged(key: String) {
        scope.launch {
            refreshKey.emit(key)
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> get(setting: Setting<T>): T {
        return when {
            setting == Setting.TimerEnabled -> dataSource.getBool("timer_feature_flag", false)
            setting == Setting.EditSetVersion -> dataSource.getInt("edit_set_screen_version", 1)
            setting == Setting.DeviceId -> getOrCreateDeviceId()
            setting == Setting.DeviceConsent -> dataSource.getSerializable<Consent>("consent", null)
            setting == Setting.UnitOfMeasure -> Settings.UnitOfWeight(UOM.valueOf(dataSource.getString("unit_of_measure") ?: "POUNDS"))
            setting == Setting.DeviceFtux -> dataSource.getBool("ftux", false)
            setting == Setting.BackupSettingsKey -> BackupSettings(LocalDate.fromEpochDays(dataSource.getInt("last_backup_epoch_days", 0)))
            setting == Setting.Bro -> dataSource.getString("bro")?.let { LiftBro.valueOf(it) }
            setting == Setting.MerSettings -> dataSource.getSerializable<MERSettings>("mer_settings", null)
                ?: MERSettings(enabled = isUserProDefault())
            setting == Setting.LatestReadReleaseNotes -> dataSource.getString("latest_read_release_notes")
            setting == Setting.ThemeModeKey -> dataSource.getString("theme_mode")?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            setting == Setting.EMaxEnabled -> dataSource.getBool("emax_enabled", isUserProDefault())
            setting == Setting.TMaxEnabled -> dataSource.getBool("tmax_enabled", isUserProDefault())
            setting == Setting.ClientUrl -> dataSource.getString("remote_client_url", null)
            setting == Setting.ShowTotalWeightMoved -> dataSource.getBool("show_twm", isUserProDefault())
            else -> throw IllegalArgumentException("Unknown setting: $setting")
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> set(setting: Setting<T>, value: T) {
        when {
            setting == Setting.TimerEnabled -> {
                dataSource.putBool("timer_feature_flag", value as Boolean)
                keyChanged("timer_feature_flag")
            }
            setting == Setting.EditSetVersion -> {
                dataSource.putInt("edit_set_screen_version", value as Int)
                keyChanged("edit_set_screen_version")
            }
            setting == Setting.DeviceId -> {}
            setting == Setting.DeviceConsent -> {
                dataSource.putSerializable("consent", value as Consent?)
                keyChanged("consent")
            }
            setting == Setting.UnitOfMeasure -> {
                dataSource.putString("unit_of_measure", (value as Settings.UnitOfWeight).uom.toString())
                keyChanged("unit_of_measure")
            }
            setting == Setting.DeviceFtux -> {
                dataSource.putBool("ftux", value as Boolean)
                keyChanged("ftux")
            }
            setting == Setting.BackupSettingsKey -> {
                dataSource.putInt("last_backup_epoch_days", (value as BackupSettings).lastBackupDate.toEpochDays())
                keyChanged("last_backup_epoch_days")
            }
            setting == Setting.Bro -> {
                dataSource.putString("bro", (value as LiftBro?)?.name)
                keyChanged("bro")
            }
            setting == Setting.MerSettings -> {
                dataSource.putSerializable("mer_settings", value as MERSettings)
                keyChanged("mer_settings")
            }
            setting == Setting.LatestReadReleaseNotes -> {
                dataSource.putString("latest_read_release_notes", value as String?)
                keyChanged("latest_read_release_notes")
            }
            setting == Setting.ThemeModeKey -> {
                dataSource.putString("theme_mode", (value as ThemeMode).toString())
                keyChanged("theme_mode")
            }
            setting == Setting.EMaxEnabled -> {
                dataSource.putBool("emax_enabled", value as Boolean)
                keyChanged("emax_enabled")
            }
            setting == Setting.TMaxEnabled -> {
                dataSource.putBool("tmax_enabled", value as Boolean)
                keyChanged("tmax_enabled")
            }
            setting == Setting.ClientUrl -> {
                dataSource.putString("remote_client_url", value as String?)
                keyChanged("remote_client_url")
            }
            setting == Setting.ShowTotalWeightMoved -> {
                dataSource.putBool("show_twm", value as Boolean)
                keyChanged("show_twm")
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T> listen(setting: Setting<T>): Flow<T> {
        val key = when {
            setting == Setting.TimerEnabled -> "timer_feature_flag"
            setting == Setting.EditSetVersion -> "edit_set_screen_version"
            setting == Setting.DeviceId -> "device_id"
            setting == Setting.DeviceConsent -> "consent"
            setting == Setting.UnitOfMeasure -> "unit_of_measure"
            setting == Setting.DeviceFtux -> "ftux"
            setting == Setting.BackupSettingsKey -> "last_backup_epoch_days"
            setting == Setting.Bro -> "bro"
            setting == Setting.MerSettings -> "mer_settings"
            setting == Setting.LatestReadReleaseNotes -> "latest_read_release_notes"
            setting == Setting.ThemeModeKey -> "theme_mode"
            setting == Setting.EMaxEnabled -> "emax_enabled"
            setting == Setting.TMaxEnabled -> "tmax_enabled"
            setting == Setting.ClientUrl -> "remote_client_url"
            setting == Setting.ShowTotalWeightMoved -> "show_twm"
            else -> throw IllegalArgumentException("Unknown setting: $setting")
        }
        
        return refreshKey
            .filter { it == key }
            .map { get(setting) }
            .onStart {
                emit(get(setting))
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
            false
        }
    }

    private suspend fun isUserProAsync(): Boolean {
        return try {
            Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.contains("pro")
        } catch (e: Exception) {
            false
        }
    }
}
