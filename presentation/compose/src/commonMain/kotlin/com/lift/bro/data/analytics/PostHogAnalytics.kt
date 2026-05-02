package com.lift.bro.data.analytics

import com.lift.bro.core.buildconfig.BuildKonfig
import com.lift.bro.domain.analytics.Analytics
import io.github.samuolis.posthog.PostHog
import io.github.samuolis.posthog.PostHogConfig
import io.github.samuolis.posthog.PostHogContext

class PostHogAnalytics(
    config: PostHogConfig,
    context: PostHogContext,
) : Analytics {

    init {
        PostHog.setup(config, context)
    }

    private val enabled: Boolean = BuildKonfig.POSTHOG_API_KEY.isNotBlank()

    override fun trackScreenView(screenName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.screen(screenName = screenName, properties = properties)
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any>) {
        if (!enabled) return
        PostHog.capture(event = eventName, properties = properties)
    }

    override fun setUserProperty(name: String, value: Any) {
        if (!enabled) return
        val distinctId = PostHog.getDistinctId() ?: return
        PostHog.identify(distinctId = distinctId, userProperties = mapOf(name to value))
    }

    override fun setUserId(userId: String?) {
        if (!enabled) return
        if (userId != null) {
            PostHog.identify(distinctId = userId)
        } else {
            PostHog.reset()
        }
    }
}
