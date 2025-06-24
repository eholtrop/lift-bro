package com.lift.bro.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.SharedPreferencesSettingsRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init
import io.github.vinceglb.filekit.manualFileKitCoreInitialization

actual class DependencyContainer {

    companion object {
        private var context: Context? = null

        fun initialize(context: Context) {
            this.context = context
            FileKit.init(context as ComponentActivity)
        }
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

    actual fun launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context?.startActivity(intent)
    }

    actual fun launchManageSubscriptions() {
        context?.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://play.google.com/store/account/subscriptions?sku=pro&package=com.lift.bro")
            )
        )

    }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }