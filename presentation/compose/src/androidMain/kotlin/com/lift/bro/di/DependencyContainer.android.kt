package com.lift.bro.di

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import com.lift.bro.audio.AndroidAudioPlayer
import com.lift.bro.audio.AudioPlayer
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.data.DriverFactory
import com.lift.bro.data.LBDatabase
import com.lift.bro.data.analytics.NoOpAnalytics
import com.lift.bro.data.analytics.PostHogAnalytics
import com.lift.bro.data.datasource.UserPreferencesDataSource
import com.lift.bro.data.repository.SettingsRepository
import com.lift.bro.domain.analytics.Analytics
import com.lift.bro.domain.repositories.ISettingsRepository
import io.github.samuolis.posthog.PostHogConfig
import io.github.samuolis.posthog.PostHogContext
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
        val driverFactory = DriverFactory(context!!)
        LBDatabase(driverFactory)
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

    actual val audioPlayer: AudioPlayer by lazy {
        AndroidAudioPlayer(context!!)
    }

    actual val analytics: Analytics by lazy {
        if (BuildKonfig.POSTHOG_API_KEY.isBlank()) return@lazy NoOpAnalytics()
        PostHogAnalytics(
            config = PostHogConfig(
                apiKey = BuildKonfig.POSTHOG_API_KEY,
                host = "https://us.i.posthog.com"
            ),
            context = PostHogContext(context?.applicationContext as Application)
        )
    }
}

actual val dependencies: DependencyContainer by lazy { DependencyContainer() }
