package com.lift.bro.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.Coordinator
import com.lift.bro.presentation.variation.UOM
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

actual class DependencyContainer {

    companion object {
        var context: Context? = null
    }

    actual val database: LBDatabase by lazy {
        LBDatabase(DriverFactory(context!!))
    }

    actual fun launchCalculator() {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_APP_CALCULATOR)
        context?.startActivity(intent)
    }

    actual val settingsRepository: ISettingsRepository
        get() = object : ISettingsRepository {

            private val sharedPreferences by lazy {
                context!!.getSharedPreferences("lift.bro.prefs", MODE_PRIVATE)
            }

            override fun getUnitOfMeasure(): Flow<Settings.UnitOfWeight> {
                val state: MutableStateFlow<Settings.UnitOfWeight> = MutableStateFlow(
                    Settings.UnitOfWeight(
                        UOM.valueOf(
                            sharedPreferences!!.getString(
                                "unit_of_measure",
                                UOM.POUNDS.toString()
                            )!!
                        )
                    )
                )

                val listener =
                    SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, key ->
                        if (key == "unit_of_measure") {
                            state.tryEmit(
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

                return state.asStateFlow()
                    .onStart {
                        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
                    }
                    .onCompletion {
                        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
                    }
            }

            override fun saveUnitOfMeasure(uom: Settings.UnitOfWeight) {
                sharedPreferences.edit().putString("unit_of_measure", uom.uom.toString()).apply()
            }

            override fun getBackupSettings(): Flow<BackupSettings> {
                // TODO: update this to listen to changes instead of just fetching once... for now this is fine!
                return flow {
                    emit(
                        BackupSettings(
                            LocalDate.fromEpochDays(
                                sharedPreferences.getInt(
                                    "last_backup_epoch_days", Clock.System.todayIn(
                                        TimeZone.currentSystemDefault()
                                    ).toEpochDays()
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

        }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }