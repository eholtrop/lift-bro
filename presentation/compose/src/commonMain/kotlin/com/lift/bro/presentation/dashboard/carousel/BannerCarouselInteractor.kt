package com.lift.bro.presentation.dashboard.carousel

import androidx.compose.runtime.Composable
import com.lift.bro.di.dependencies
import com.lift.bro.domain.analytics.Analytics
import com.lift.bro.domain.models.settings.AnalyticsConsent
import com.lift.bro.domain.repositories.ISettingsRepository
import com.lift.bro.domain.repositories.Setting
import com.lift.bro.ui.ReleaseNote
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import lift_bro.core.generated.resources.Res
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor

typealias DashboardBannerCarouselInteractor = Interactor<DashboardBannerCarouselState, DashboardBannerEvent>

@Composable
fun rememberBannerCarouselInteractor(
    settingsRepository: ISettingsRepository = dependencies.settingsRepository,
    analytics: Analytics = dependencies.analytics,
): DashboardBannerCarouselInteractor = rememberInteractor(
    initialState = DashboardBannerCarouselState(),
    source = {
        combine(
            settingsRepository.listen(Setting.AnalyticsConsent),
            settingsRepository.listen(Setting.LatestReadReleaseNotes),
            flow {
                emit(
                    Json.decodeFromString<List<ReleaseNote>>(Res.readBytes("files/release_notes.json").decodeToString())
                )
            }
        ) { consent, lastReadReleaseNotes, releaseNotes ->
            DashboardBannerCarouselState(
                latestReleaseNote = releaseNotes.maxOfOrNull { it.versionId },
                banners = listOfNotNull(
                    if (releaseNotes.maxOfOrNull { it.versionId } != lastReadReleaseNotes) {
                        DashboardBanner.ReleaseNotes(
                            releaseNotes
                        )
                    } else {
                        null
                    },
                    if (!consent.dashboardBannerDismissed) DashboardBanner.AnalyticsConsent else null,
                )
            )
        }
    },
    sideEffects = listOf(
        SideEffect { _, state, event ->
            when (event) {
                DashboardBannerEvent.DismissAnalyticsBanner -> {
                    settingsRepository.set(
                        Setting.AnalyticsConsent,
                        AnalyticsConsent(true, false)
                    )
                    analytics.setConsent(false)
                }

                DashboardBannerEvent.ReleaseNotesSeen, DashboardBannerEvent.DismissReleaseNotes -> {
                    settingsRepository.set(
                        Setting.LatestReadReleaseNotes,
                        state.latestReleaseNote,
                    )
                }

                DashboardBannerEvent.EnableAnalytics -> {
                    settingsRepository.set(
                        Setting.AnalyticsConsent,
                        AnalyticsConsent(true, true)
                    )
                    analytics.setConsent(true)
                }
            }
        }
    )
)
