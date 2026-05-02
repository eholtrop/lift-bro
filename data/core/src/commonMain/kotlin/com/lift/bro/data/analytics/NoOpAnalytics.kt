package com.lift.bro.data.analytics

import com.lift.bro.domain.analytics.Analytics

class NoOpAnalytics : Analytics {

    override fun setConsent(consented: Boolean) = Unit

    override fun trackScreenView(screenName: String, properties: Map<String, Any>) = Unit

    override fun trackEvent(eventName: String, properties: Map<String, Any>) = Unit

    override fun setUserProperty(name: String, value: Any) = Unit

    override fun setUserId(userId: String?) = Unit
}
