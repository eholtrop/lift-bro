package com.lift.bro.presentation.dashboard.carousel

import com.lift.bro.ui.ReleaseNote
import kotlinx.serialization.Serializable

@Serializable
data class DashboardBannerCarouselState(
    val banners: List<DashboardBanner> = emptyList(),
)

@Serializable
sealed class DashboardBanner {
    @Serializable
    data class ReleaseNotes(
        val notes: List<ReleaseNote>,
    ): DashboardBanner()

    @Serializable
    data object AnalyticsConsent: DashboardBanner()
}

sealed interface DashboardBannerEvent {
    data object EnableAnalytics: DashboardBannerEvent
    data object DismissAnalyticsBanner: DashboardBannerEvent

}
