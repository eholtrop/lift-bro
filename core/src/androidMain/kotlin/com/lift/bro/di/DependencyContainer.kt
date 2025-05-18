package com.lift.bro.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.SharedPreferencesSettingsRepository
import com.lift.bro.domain.models.Settings
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.models.UOM
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
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

    actual val settingsRepository: ISettingsRepository by lazy {
        SharedPreferencesSettingsRepository(context!!)
    }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }