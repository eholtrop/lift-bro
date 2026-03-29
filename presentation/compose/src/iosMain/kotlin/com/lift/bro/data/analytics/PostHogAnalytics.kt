package com.lift.bro.data.analytics

import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.analytics.Analytics
import com.posthog.PostHog
import com.posthog.PostHogConfig
import com.posthog.PropertyObject

actual class PostHogAnalytics : Analytics {

    private var enabled: Boolean = BuildKonfig.POSTHOG_API_KEY.isNotBlank()

    init {
        val config = PostHogConfig(
            apiKey = BuildKonfig.POSTHOG_API_KEY,
            host = POSTHOG_HOST
        )
        PostHog.setup(config = config)
    }

    override fun trackScreenView(screenName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.screen(screenTitle = screenName, properties = properties.toPropertyObject())
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.capture(event = eventName, properties = properties.toPropertyObject())
    }

    override fun setUserProperty(name: String, value: Any) {
        if (!enabled) return
        val properties = mapOf(name to value).toPropertyObject()
        PostHog.identify(distinctId = PostHog.distinctId(), userProperties = properties)
    }

    override fun setUserId(userId: String?) {
        if (!enabled) return
        if (userId != null) {
            PostHog.identify(distinctId = userId)
        } else {
            PostHog.reset()
        }
    }

    private fun Map<String, Any?>.toPropertyObject(): PropertyObject {
        return PropertyObject().also { obj ->
            this.forEach { (key, value) ->
                when (value) {
                    is String -> obj[key] = value
                    is Number -> obj[key] = value
                    is Boolean -> obj[key] = value
                    null -> obj[key] = null
                    else -> obj[key] = value.toString()
                }
            }
        }
    }

    companion object {
        private const val POSTHOG_HOST = "https://app.posthog.com"
    }
}
