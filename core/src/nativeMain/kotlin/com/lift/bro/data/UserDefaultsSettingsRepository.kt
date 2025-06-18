package com.lift.bro.data

import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.models.UOM
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.presentation.onboarding.LiftBro
import com.lift.bro.utils.debug
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
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
            key = "backup_settings",
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

    override fun shouldShowMerCalcs(): Flow<Boolean> {
        return subscribeToKey(
            key = "show_mer_calcs",
            block = { key ->
                userDefaults.boolForKey(key)
            }
        )
    }

    override fun setShowMerCalcs(showMerCalcs: Boolean) {
        userDefaults.setBool(showMerCalcs, "show_mer_calcs")
        keyChanged("show_mer_calcs")
    }
}