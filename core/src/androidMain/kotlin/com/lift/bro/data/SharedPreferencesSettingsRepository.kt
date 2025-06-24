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

    override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
        return keyChangedFlow
            .filter { it == "unit_of_measure" }
            .map {
                val uom = sharedPreferences!!.getString(
                    "unit_of_measure",
                    UOM.POUNDS.toString()
                )!!
                Settings.UnitOfWeight(
                    UOM.valueOf(
                        uom
                    )
                )
            }
            .onStart {
                emit(
                    Settings.UnitOfWeight(
                        UOM.valueOf(
                            sharedPreferences!!.getString(
                                "unit_of_measure",
                                UOM.POUNDS.toString()
                            )!!
                        )
                    )
                )
            }
    }

    override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {
        sharedPreferences.edit { putString("unit_of_measure", uom.uom.toString()) }
    }

    override fun getDeviceFtux(): Flow<Boolean> {
        return keyChangedFlow
            .filter { it == "ftux" }
            .map {
                sharedPreferences!!.getBoolean("ftux", false)
            }.onStart {
                emit(sharedPreferences!!.getBoolean("ftux", false))
            }
    }

    override fun setDeviceFtux(ftux: Boolean) {
        sharedPreferences.edit { putBoolean("ftux", ftux) }
    }

    override fun getBackupSettings(): Flow<BackupSettings> {
        return flow {
            emit(
                BackupSettings(
                    LocalDate.fromEpochDays(
                        sharedPreferences.getInt(
                            "last_backup_epoch_days", 0
                        )
                    )
                )
            )
        }
    }

    override fun saveBackupSettings(settings: BackupSettings) {
        sharedPreferences.edit().apply {
            this.putInt("last_backup_epoch_days", settings.lastBackupDate.toEpochDays())
        }.apply()
    }

    override fun getBro(): Flow<LiftBro?> {
        return keyChangedFlow
            .filter { "bro" == it }
            .map {
                sharedPreferences.getString("bro", null)?.let {
                    LiftBro.valueOf(it)
                }
            }.onStart {
                emit(
                    sharedPreferences.getString("bro", null)?.let {
                        LiftBro.valueOf(it)
                    }
                )
            }
    }

    override fun setBro(bro: LiftBro) {
        sharedPreferences.edit { putString("bro", bro.toString()) }
    }

    override fun shouldShowMerCalcs(): Flow<Boolean> {
        return keyChangedFlow
            .filter { it == "show_mer_calcs" }
            .map {
                sharedPreferences.getBoolean("show_mer_calcs", false)
            }.onStart {
                emit(
                    sharedPreferences.getBoolean("show_mer_calcs", false)
                )
            }
    }

    override fun setShowMerCalcs(showMerCalcs: Boolean) {
        sharedPreferences.edit { putBoolean("show_mer_calcs", showMerCalcs) }
    }

    override fun getLatestReadReleaseNotes(): Flow<String?> {
        return keyChangedFlow
            .filter { "latest_read_release_notes" == it }
            .map {
                sharedPreferences.getString("latest_read_release_notes", null)
            }.onStart {
                emit(
                    sharedPreferences.getString("latest_read_release_notes", null)
                )
            }
    }

    override fun setLatestReadReleaseNotes(versionId: String) {
        sharedPreferences.edit { putString("latest_read_release_notes", versionId) }
    }

    override fun getThemeMode(): Flow<ThemeMode> {
        return keyChangedFlow
            .filter { "theme_mode" == it }
            .map {
                sharedPreferences.getString("theme_mode", null)?.let {
                    ThemeMode.valueOf(it)
                } ?: ThemeMode.System
            }.onStart {
                emit(
                    sharedPreferences.getString("theme_mode", null)?.let {
                        ThemeMode.valueOf(it)
                    } ?: ThemeMode.System
                )
            }
            .debug("DEBUGEH")
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit { putString("theme_mode", themeMode.toString()) }
    }
}