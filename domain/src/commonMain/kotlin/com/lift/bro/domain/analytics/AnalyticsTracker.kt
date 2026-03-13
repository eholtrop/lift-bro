package com.lift.bro.domain.analytics

interface AnalyticsTracker {
    fun trackScreenView(screenName: String, properties: Map<String, Any?> = emptyMap())

    fun trackEvent(eventName: String, properties: Map<String, Any?> = emptyMap())

    fun setUserProperty(name: String, value: Any?)

    fun setUserId(userId: String?)

    fun isEnabled(): Boolean
}
