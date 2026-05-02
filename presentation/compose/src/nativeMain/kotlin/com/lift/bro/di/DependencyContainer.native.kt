package com.lift.bro.di

import com.lift.bro.audio.AudioPlayer
import com.lift.bro.audio.AudioPlayerImpl
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
import platform.Foundation.NSURL
import platform.UIKit.UIApplication

actual class DependencyContainer {

    actual val database: LBDatabase by lazy {
        LBDatabase(DriverFactory(Unit))
    }

    actual val settingsRepository: ISettingsRepository by lazy {
        SettingsRepository(UserPreferencesDataSource())
    }

    actual fun launchUrl(url: String) {
        val nsUrl = NSURL.URLWithString(url)
        if (nsUrl != null) {
            UIApplication.sharedApplication.openURL(
                url = nsUrl,
                options = emptyMap<Any?, Any>(),
                completionHandler = null
            )
        }
    }

    actual fun launchManageSubscriptions() {
    }

    actual val audioPlayer: AudioPlayer by lazy {
        AudioPlayerImpl()
    }

    actual val analytics: Analytics by lazy {
        if (BuildKonfig.POSTHOG_API_KEY.isBlank()) return@lazy NoOpAnalytics()
        val config = PostHogConfig(
            apiKey = BuildKonfig.POSTHOG_API_KEY,
            host = "https://us.i.posthog.com"
        )
        PostHogAnalytics(
            config = config,
            context = PostHogContext()
        )
    }
}

actual val dependencies: DependencyContainer by lazy {
    DependencyContainer()
}
