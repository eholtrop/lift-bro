package com.lift.bro.data.analytics

import android.content.Context
import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.analytics.Analytics
import com.posthog.PostHog
import com.posthog.android.PostHogAndroid
import com.posthog.android.PostHogAndroidConfig

actual class PostHogAnalytics(
    private val context: Context,
): Analytics {

    private val enabled: Boolean = BuildKonfig.POSTHOG_API_KEY.isNotBlank()

    init {
        val config = PostHogAndroidConfig(
            apiKey = BuildKonfig.POSTHOG_API_KEY,
            host = POSTHOG_HOST
        )
        PostHogAndroid.setup(context, config)
        PostHog.setup(config)
    }

    override fun trackScreenView(screenName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.screen(screenName, properties = properties)
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.capture(eventName, properties = properties)
    }

    override fun setUserProperty(name: String, value: Any) {
        if (!enabled) return
        val properties = mapOf(name to value)
        PostHog.identify(PostHog.distinctId(), userPropertiesSetOnce = properties)
    }

    override fun setUserId(userId: String?) {
        if (!enabled) return
        if (userId != null) {
            PostHog.identify(userId)
        } else {
            PostHog.reset()
        }
    }

    companion object {
        private const val POSTHOG_HOST = "https://us.i.posthog.com"
    }
}
