package com.lift.bro.data.analytics

import com.lift.bro.domain.analytics.AnalyticsTracker

class FirebaseAnalyticsTracker : AnalyticsTracker {
    override fun trackScreenView(screenName: String, properties: Map<String, Any?>) {
    }

    override fun trackEvent(eventName: String, properties: Map<String, Any?>) {
    }

    override fun setUserProperty(name: String, value: Any?) {
    }

    override fun setUserId(userId: String?) {
    }

    override fun isEnabled(): Boolean = false
}
