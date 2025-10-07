package com.lift.bro.di

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.datasource.UserPreferencesDataSource
import com.lift.bro.data.repository.SettingsRepository
import com.lift.bro.domain.repositories.ISettingsRepository
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.init


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
        SettingsRepository(UserPreferencesDataSource(context!!))
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

    actual fun startPresentationServerService(port: Int) {
        val ctx = context ?: return
        val intent = Intent(ctx, com.lift.bro.server.LiftBroServerForegroundService::class.java).apply {
            putExtra(com.lift.bro.server.LiftBroServerForegroundService.EXTRA_PORT, port)
            action = com.lift.bro.server.LiftBroServerForegroundService.ACTION_START
        }
        ctx.startForegroundService(intent)
    }

    actual fun stopPresentationServerService() {
        val ctx = context ?: return
        val intent = Intent(ctx, com.lift.bro.server.LiftBroServerForegroundService::class.java).apply {
            action = com.lift.bro.server.LiftBroServerForegroundService.ACTION_STOP
        }
        ctx.startService(intent)
    }

}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }
