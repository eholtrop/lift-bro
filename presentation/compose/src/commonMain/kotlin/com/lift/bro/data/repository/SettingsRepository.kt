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
        dataSource.putInt("last_backup_epoch_days", settings.lastBackupDate.toEpochDays())
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
                dataSource.getSerializable<MERSettings>("mer_settings", null)
                    ?: MERSettings(enabled = Purchases.sharedInstance.isUserPro())
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