package com.lift.bro.data

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.compose.ThemeMode
import com.lift.bro.domain.models.MERSettings
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.Consent
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.onboarding.LiftBro
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.LocalDate
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID


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

    // get id, if null generate a new one, store it, and return it
    override fun getDeviceId(): String {
        return sharedPreferences.getString("device_id", null) ?: UUID.randomUUID().toString().also {
            sharedPreferences.edit { putString("device_id", it) }
        }
    }

    override fun getDeviceConsent(): Flow<Consent?> {
        return keyChangedFlow
            .filter { it == "device_consent" }
            .map {
                sharedPreferences.getString("device_consent", null)?.let {
                    Json.decodeFromString<Consent>(it)
                }
            }
            .onStart {
                emit(
                    sharedPreferences.getString("device_consent", null)?.let {
                        Json.decodeFromString<Consent>(it)
                    }
                )
            }
    }

    override fun setDeviceConsent(consent: Consent) {
        sharedPreferences.edit {
            putString("device_consent", Json.encodeToString(consent))
        }
        keyChangedChannel.tryEmit("device_consent")
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
        keyChangedChannel.tryEmit("unit_of_measure")
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
        keyChangedChannel.tryEmit("ftux")
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
        sharedPreferences.edit {
            this.putInt("last_backup_epoch_days", settings.lastBackupDate.toEpochDays())
        }
        keyChangedChannel.tryEmit("last_backup_epoch_days")
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
        keyChangedChannel.tryEmit("bro")
    }

    override fun getMerSettings(): Flow<MERSettings> {
        return keyChangedFlow
            .filter { it == "mer_settings" }
            .map {
                sharedPreferences.getString("mer_settings", null)?.let {
                    try {
                        Json.decodeFromString<MERSettings>(it)
                    } catch (e: Exception) {
                        null
                    }
                } ?: MERSettings()
            }.onStart {
                emit(
                    sharedPreferences.getString("mer_settings", null)?.let {
                        try {
                            Json.decodeFromString<MERSettings>(it)
                        } catch (e: Exception) {
                            null
                        }
                    } ?: MERSettings()
                )
            }
    }

    override fun setMerSettings(settings: MERSettings) {
        sharedPreferences.edit { putString("mer_settings", Json.encodeToString(settings)) }
        keyChangedChannel.tryEmit("mer_settings")
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
        keyChangedChannel.tryEmit("latest_read_release_notes")
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
    }

    override fun setThemeMode(themeMode: ThemeMode) {
        sharedPreferences.edit { putString("theme_mode", themeMode.toString()) }
        keyChangedChannel.tryEmit("theme_mode")
    }
}