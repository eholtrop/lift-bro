package com.lift.bro.presentation.dashboard.carousel

import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.compose.ui.zIndex
import com.lift.bro.ui.AnalyticsConsentBanner
import com.lift.bro.ui.ReleaseNotesRow
import com.lift.bro.ui.theme.spacing
import kotlin.math.absoluteValue
import kotlin.math.sign

val LocalDashboardBannerCarouselInteractor = staticCompositionLocalOf<DashboardBannerCarouselInteractor?> { null }

@Composable
fun DashboardBannerCarousel(
    interactor: DashboardBannerCarouselInteractor? = null,
) {
    val resolvedInteractor = interactor
        ?: LocalDashboardBannerCarouselInteractor.current
        ?: rememberBannerCarouselInteractor()
    val state by resolvedInteractor.state.collectAsState()
    DashboardBannerCarousel(
        state = state,
        onEvent = { resolvedInteractor(it) }
    )
}

@Composable
fun DashboardBannerCarousel(
    state: DashboardBannerCarouselState,
    onEvent: (DashboardBannerEvent) -> Unit,
) {
    val pagerState = rememberPagerState { state.banners.size }

    HorizontalPager(
        state = pagerState,
        pageSpacing = 0.dp,
        contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.half),
        snapPosition = SnapPosition.Center,
        beyondViewportPageCount = state.banners.size,
    ) { page ->
        val pageOffset = (pagerState.currentPage - page) + pagerState.currentPageOffsetFraction

        val animationModifier = Modifier
            .zIndex(1f - pageOffset.absoluteValue)
            .graphicsLayer {
                alpha = lerp(
                    start = .4f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue
                )
                val nativeTranslationX = pageOffset * size.width

                val peekDistance = 60.dp.toPx()

                val fanTranslationX = -pageOffset.sign * peekDistance * pageOffset.absoluteValue.coerceIn(0f, 1f)

                translationX = nativeTranslationX + fanTranslationX

                lerp(
                    start = 0.9f,
                    stop = 1f,
                    fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f),
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }
            }

        when (state.banners[page]) {
            DashboardBanner.AnalyticsConsent -> AnalyticsConsentBanner(
                modifier = animationModifier,
                onDismiss = { onEvent(DashboardBannerEvent.DismissAnalyticsBanner) },
                onEnable = { onEvent(DashboardBannerEvent.EnableAnalytics) }
            )

            is DashboardBanner.ReleaseNotes -> ReleaseNotesRow(
                modifier = animationModifier,
                dialogSeen = { onEvent(DashboardBannerEvent.ReleaseNotesSeen) },
                rowDismissed = { onEvent(DashboardBannerEvent.DismissReleaseNotes) },
            )
        }
    }
}
