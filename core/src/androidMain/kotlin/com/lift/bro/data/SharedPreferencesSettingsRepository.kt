package com.lift.bro.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.compose.ThemeMode
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.utils.debug
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate

class SharedPreferencesSettingsRepository(
    val context: Context,
) : ISettingsRepository {

    private val sharedPreferences by lazy {
        context.getSharedPreferences("lift.bro.prefs", MODE_PRIVATE)
    }

    private val keyChangedChannel: MutableSharedFlow<String> =
        MutableSharedFlow(extraBufferCapacity = 1)

    val preferenceListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
            if (key != null) {
                keyChangedChannel.tryEmit(key)
            }
        }

    private val keyChangedFlow = keyChangedChannel
        .onStart {
            sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceListener)
        }.onCompletion {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(preferenceListener)
        }

    private fun <R> subscribeToKey(key: String, block: (String) -> R): Flow<R> {
        return keyChangedChannel
            .filter { it == key }
            .map(block)
            .onStart {
                emit(block(key))
            }
    }

    override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
        return subscribeToKey(
            key = "unit_of_measure",
            block = {
                Settings.UnitOfWeight(
                    UOM.valueOf(
                        sharedPreferences!!.getString(
                            it,
                            UOM.POUNDS.toString()
                        )!!
                    )
                )
            }
        )
    }

    override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {
        sharedPreferences.edit { putString("unit_of_measure", uom.uom.toString()) }
    }

    override fun getDeviceFtux(): Flow<Boolean> {
        return subscribeToKey(
            key = "ftux",
            block = {
                sharedPreferences!!.getBoolean("ftux", false)
            }
        )
    }

    override fun setDeviceFtux(ftux: Boolean) {
        sharedPreferences.edit { putBoolean("ftux", ftux) }
    }

    override fun getBackupSettings(): Flow<BackupSettings> {
        return subscribeToKey(
            key = "last_backup_epoch_days",
            block = {
                BackupSettings(
                    LocalDate.fromEpochDays(
                        sharedPreferences.getInt(
                            "last_backup_epoch_days", 0
                        )
                    )
                )
            }
        )
    }

    override fun saveBackupSettings(settings: BackupSettings) {
        sharedPreferences.edit {
            this.putInt("last_backup_epoch_days", settings.lastBackupDate.toEpochDays())
        }
    }

    override fun getBro(): Flow<LiftBro?> {
        return subscribeToKey(
            key = "bro",
            block = {
                sharedPreferences.getString(it, null)?.let { LiftBro.valueOf(it) }
            }
        )
    }

    override fun setBro(bro: LiftBro) {
        sharedPreferences.edit { putString("bro", bro.toString()) }
    }

    override fun shouldShowMerCalcs(): Flow<Boolean> {
        return subscribeToKey(
            key = "show_mer_calcs",
            block = {
                sharedPreferences.getBoolean(it, false)
            }
        )
    }

    override fun setShowMerCalcs(showMerCalcs: Boolean) {
        sharedPreferences.edit { putBoolean("show_mer_calcs", showMerCalcs) }
    }

    override fun getLatestReadReleaseNotes(): Flow<String?> {
        return subscribeToKey(
            key = "latest_read_release_notes",
            block = {
                sharedPreferences.getString(it, null)
            }
        )
    }

    override fun setLatestReadReleaseNotes(versionId: String) {
        sharedPreferences.edit { putString("latest_read_release_notes", versionId) }
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return subscribeToKey(
            key = "theme_mode",
            block = {
                sharedPreferences.getString("theme_mode", null)?.let {
                    ThemeMode.valueOf(it)
                } ?: ThemeMode.System
            }
        )
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit { putString("theme_mode", themeMode.toString()) }
    }
}