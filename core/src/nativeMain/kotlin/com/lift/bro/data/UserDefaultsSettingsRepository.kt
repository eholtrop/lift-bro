package com.lift.bro.data

import com.benasher44.uuid.uuid4
import com.example.compose.ThemeMode
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

class UserDefaultsSettingsRepository : ISettingsRepository {

    private val userDefaults = NSUserDefaults.standardUserDefaults

    private val refreshKey by lazy { MutableSharedFlow<String>() }

    private fun keyChanged(key: String) {
        GlobalScope.launch {
            refreshKey.emit(key)
        }
    }

    private fun <R> subscribeToKey(key: String, block: (String) -> R): Flow<R> {
        return refreshKey
            .filter { it == key }
            .map(block)
            .onStart {
                emit(block(key))
            }
    }

    override fun getDeviceId(): String {
        return userDefaults.stringForKey("device_id") ?: uuid4().toString().also {
            userDefaults.setObject(it, "device_id")
        }
    }

    override fun getDeviceConsent(): Flow<Consent?> {
        return subscribeToKey(
            key = "consent",
            block = { key ->
                Log.d("", key)
                userDefaults.stringForKey(key)?.let {
                    Log.d("", it)
                    Json.decodeFromString<Consent>(it)
                }
            }
        )
    }

    override fun setDeviceConsent(consent: Consent) {
        Log.d("", Json.encodeToString(consent))
        userDefaults.setObject(Json.encodeToString(consent), "consent")
        keyChanged("consent")
    }

    override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
        return subscribeToKey(
                key = "unit_of_measure",
                block = { key ->
                    Settings.UnitOfWeight(UOM.valueOf(userDefaults.stringForKey(key) ?: "POUNDS"))
                }
            )
    }

    override fun getDeviceFtux(): Flow<Boolean> {
        return subscribeToKey(
            key = "ftux",
            block = { key ->
                userDefaults.boolForKey("ftux")
            }
        )
    }

    override fun setDeviceFtux(ftux: Boolean) {
        userDefaults.setObject(ftux, "ftux")
        keyChanged("ftux")
    }

    override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {
        userDefaults.setObject(uom.uom.toString(), "unit_of_measure")
        keyChanged("unit_of_measure")
    }

    override fun getBackupSettings(): Flow<BackupSettings> {
        return subscribeToKey(
            key = "last_backup_epoch_days",
            block = { key ->
                BackupSettings(
                    lastBackupDate = LocalDate.fromEpochDays(userDefaults.integerForKey(key).toInt())
                )
            }
        )
    }

    override fun saveBackupSettings(settings: BackupSettings) {

    }

    override fun getBro(): Flow<LiftBro?> {
        return subscribeToKey(
            key = "bro",
            block = {
                userDefaults.stringForKey("bro")?.let { LiftBro.valueOf(it) }
            }
        )
    }

    override fun setBro(bro: LiftBro) {
        userDefaults.setObject(bro.name, "bro")
        keyChanged("bro")
    }

    override fun getMerSettings(): Flow<MERSettings> {
        return subscribeToKey(
            key = "mer_settings",
            block = { key ->
                with (userDefaults.stringForKey("mer_settings")) {
                    if (this != null) {
                        Json.decodeFromString<MERSettings>(this)
                    } else {
                        MERSettings()
                    }
                }
            }
        )
    }

    override fun setMerSettings(merSettings: MERSettings) {
        userDefaults.setObject(Json.encodeToString(merSettings), "mer_settings")
        keyChanged("mer_settings")
    }

    override fun getLatestReadReleaseNotes(): Flow<String?> {
        return subscribeToKey(
            "latest_read_release_notes",
            block = { key ->
                userDefaults.stringForKey(key)
            }
        )
    }

    override fun setLatestReadReleaseNotes(versionId: String) {
        userDefaults.setObject(versionId, "latest_read_release_notes")
        keyChanged("latest_read_release_notes")
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return subscribeToKey(
            "theme_mode",
            block = { key ->
                userDefaults.stringForKey(key)?.let { ThemeMode.valueOf(it) } ?: ThemeMode.System
            }
        )
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        userDefaults.setObject(themeMode.toString(), "theme_mode")
        keyChanged("theme_mode")
    }
}