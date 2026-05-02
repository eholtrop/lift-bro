package com.lift.bro.domain.models.settings

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsConsent(
    val dashboardBannerDismissed: Boolean,
    val consented: Boolean,
)
