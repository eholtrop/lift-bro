package com.lift.bro.data.repository

import com.benasher44.uuid.uuid4
import com.lift.bro.data.datasource.UserPreferencesDataSource
import com.lift.bro.domain.models.LiftBro
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.ThemeMode
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.ktx.awaitCustomerInfo
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
) : ISettingsRepository {

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
            Setting.BackupSettings -> dataSource.putInt(key, (value as BackupSettings).lastBackupDate.toEpochDays().toInt())
            Setting.Bro -> dataSource.putString(key, (value as LiftBro).name)
            Setting.ClientUrl -> dataSource.putString(key, value as String?)
            Setting.Consent -> dataSource.putSerializable(key, value as Consent)
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
            Setting.UnitOfMeasure -> dataSource.putString(key, (value as Settings.UnitOfWeight).uom.toString())
        }
        keyChanged(key)
    }

    private val Setting<*>.key: String get() = when (this) {
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
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun <T> Setting<T>.value(): T {
        return when (this) {
            Setting.BackupSettings -> BackupSettings(
                lastBackupDate = LocalDate.fromEpochDays(dataSource.getInt(key, 0))
            ) as T
            Setting.Bro -> dataSource.getString(key, null)?.let { LiftBro.valueOf(it) }
            Setting.ClientUrl -> dataSource.getString(key, null)
            Setting.Consent -> dataSource.getSerializable<Consent>(key, null)
            Setting.DashboardV3 -> dataSource.getBool(key, false)
            Setting.DeviceFtux -> dataSource.getBool(key, false)
            Setting.EMaxEnabled -> dataSource.getBool(key, Purchases.sharedInstance.isUserPro())
            Setting.EditSetVersion -> dataSource.getInt(key, 1)
            Setting.LatestReadReleaseNotes -> dataSource.getString(key, null)
            Setting.MerSettings -> dataSource.getSerializable<MERSettings>(key, null) ?: MERSettings(enabled = Purchases.sharedInstance.isUserPro())
            Setting.ShowTotalWeightMoved -> dataSource.getBool(key, Purchases.sharedInstance.isUserPro())
            Setting.TMaxEnabled -> dataSource.getBool(key, Purchases.sharedInstance.isUserPro())
            Setting.ThemeMode -> dataSource.getString(key, null)?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            Setting.Timer -> dataSource.getBool(key, false)
            Setting.UnitOfMeasure -> Settings.UnitOfWeight(UOM.valueOf(dataSource.getString(key, "POUNDS")!!))
        } as T
    }

    override fun enableTimer(): Boolean {
        return dataSource.getBool("timer_feature_flag", false)
    }

    override fun setEnableTimer(enabled: Boolean) {
        dataSource.putBool("timer_feature_flag", enabled)
        keyChanged("timer_feature_flag")
    }

    override fun dashboardV3(): Boolean {
        return dataSource.getBool("dashboard_v3", false)
    }

    override fun enableDashboardV3(enabled: Boolean) {
        dataSource.putBool("dashboard_v3", enabled)
        keyChanged("dashboard_v3")
    }

    override fun editSetVersion(): Int {
        return dataSource.getInt("edit_set_screen_version", 1)
    }

    override fun setEditSetVersion(version: Int) {
        dataSource.putInt("edit_set_screen_version", version)
        keyChanged("edit_set_screen_version")
    }

    override fun getDeviceId(): String {
        return dataSource.getString("device_id") ?: uuid4().toString().also {
            dataSource.putString("device_id", it)
        }
    }

    override fun getDeviceConsent(): Flow<Consent?> {
        return subscribeToKey(
            key = "consent",
            block = { key ->
                dataSource.getSerializable<Consent>(key, null)
            }
        )
    }

    override fun setDeviceConsent(consent: Consent) {
        dataSource.putSerializable("consent", consent)
        keyChanged("consent")
    }

    override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
        return subscribeToKey(
            key = "unit_of_measure",
            block = { key ->
                Settings.UnitOfWeight(UOM.valueOf(dataSource.getString(key) ?: "POUNDS"))
            }
        )
    }

    override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {
        dataSource.putString("unit_of_measure", uom.uom.toString())
        keyChanged("unit_of_measure")
    }

    override fun getDeviceFtux(): Flow<Boolean> {
        return subscribeToKey(
            key = "ftux",
            block = { key ->
                dataSource.getBool("ftux", false)
            }
        )
    }

    override fun setDeviceFtux(ftux: Boolean) {
        dataSource.putBool("ftux", ftux)
        keyChanged("ftux")
    }

    override fun getBackupSettings(): Flow<BackupSettings> {
        return subscribeToKey(
            key = "last_backup_epoch_days",
            block = { key ->
                BackupSettings(
                    lastBackupDate = LocalDate.fromEpochDays(dataSource.getInt(key, 0))
                )
            }
        )
    }

    override fun saveBackupSettings(settings: BackupSettings) {
        dataSource.putInt("last_backup_epoch_days", settings.lastBackupDate.toEpochDays().toInt())
        keyChanged("last_backup_epoch_days")
    }

    override fun getBro(): Flow<LiftBro?> {
        return subscribeToKey(
            key = "bro",
            block = {
                dataSource.getString("bro")?.let { LiftBro.valueOf(it) }
            }
        )
    }

    override fun setBro(bro: LiftBro) {
        dataSource.putString("bro", bro.name)
        keyChanged("bro")
    }

    override fun getMerSettings(): Flow<MERSettings> {
        return subscribeToKey(
            key = "mer_settings",
            block = { key ->
                dataSource.getSerializable<MERSettings>("mer_settings", null) ?: MERSettings(enabled = Purchases.sharedInstance.isUserPro())
            }
        )
    }

    override fun setMerSettings(merSettings: MERSettings) {
        dataSource.putSerializable("mer_settings", merSettings)
        keyChanged("mer_settings")
    }

    override fun getLatestReadReleaseNotes(): Flow<String?> {
        return subscribeToKey(
            "latest_read_release_notes",
            block = { key ->
                dataSource.getString(key)
            }
        )
    }

    override fun setLatestReadReleaseNotes(versionId: String) {
        dataSource.putString("latest_read_release_notes", versionId)
        keyChanged("latest_read_release_notes")
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return subscribeToKey(
            "theme_mode",
            block = { key ->
                dataSource.getString(key)?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            }
        )
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        dataSource.putString("theme_mode", themeMode.toString())
        keyChanged("theme_mode")
    }

    override fun eMaxEnabled(): Flow<Boolean> {
        return subscribeToKey("emax_enabled") {
            dataSource.getBool("emax_enabled", Purchases.sharedInstance.isUserPro())
        }
    }

    override fun setEMaxEnabled(enabled: Boolean) {
        dataSource.putBool("emax_enabled", enabled)
        keyChanged("emax_enabled")
    }

    override fun tMaxEnabled(): Flow<Boolean> {
        return subscribeToKey("tmax_enabled") {
            dataSource.getBool("tmax_enabled", Purchases.sharedInstance.isUserPro())
        }
    }

    override fun setTMaxEnabled(enabled: Boolean) {
        dataSource.putBool("tmax_enabled", enabled)
        keyChanged("tmax_enabled")
    }

    override fun getClientUrl(): String? {
        return dataSource.getString("remote_client_url", null)
    }

    override fun setClientUrl(url: String?) {
        dataSource.putString("remote_client_url", url)
        keyChanged("remote_client_url")
    }

    override fun showTotalWeightMoved(show: Boolean) {
        dataSource.putBool("show_twm", show)
        keyChanged("show_twm")
    }

    override fun shouldShowTotalWeightMoved(): Flow<Boolean> {
        return subscribeToKey("show_twm") { key ->
            dataSource.getBool(key, Purchases.sharedInstance.isUserPro())
        }
    }
}

suspend fun Purchases.isUserPro() =
    Purchases.sharedInstance.awaitCustomerInfo().entitlements.active.contains("pro")
