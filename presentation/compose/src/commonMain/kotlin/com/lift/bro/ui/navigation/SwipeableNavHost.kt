package com.lift.bro.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.util.lerp
import com.lift.bro.config.BuildConfig
import com.lift.bro.utils.logger.Log
import com.lift.bro.utils.logger.d
import io.sentry.kotlin.multiplatform.Sentry
import io.sentry.kotlin.multiplatform.protocol.Breadcrumb
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

@Composable
fun SwipeableNavHost(
    modifier: Modifier = Modifier,
    navCoordinator: NavCoordinator,
    key: (Destination) -> Any = { Json.encodeToString(it) },
    content: @Composable (Destination) -> Unit,
) {
    val pages by navCoordinator.pagesAsFlow.collectAsState(emptyList())
    val currentPage by navCoordinator.currentPageAsFlow.collectAsState(null)

    val savedPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pages.size }
    )

    LaunchedEffect(currentPage) {
        if (currentPage != Destination.Unknown) {
            savedPagerState.animateScrollToPage(
                page = pages.indexOf(currentPage)
            )
        }
    }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(savedPagerState.currentPage) {
        navCoordinator.updateCurrentIndex(savedPagerState.currentPage)
        keyboard?.hide()
    }
    LaunchedEffect(savedPagerState.currentPage) {
        if (!BuildConfig.isDebug) {
            Sentry.addBreadcrumb(Breadcrumb.navigation("unknown", navCoordinator.currentPage.toString()))
        }
    }

    var pagerSize: Size? by remember { mutableStateOf(null) }

    HorizontalPager(
        modifier = modifier.graphicsLayer {
            pagerSize = this.size
        },
        key = {
            key(pages[it])
        },
        state = savedPagerState,
    ) { currentPage ->
        // Animation to make the pages feel like they are disappearing to the side
        val animationModifier = Modifier.graphicsLayer {
            val pageOffset =
                ((savedPagerState.currentPage - currentPage) + savedPagerState.currentPageOffsetFraction)

            // page moving end to start
            if (pageOffset <= 1) {
                lerp(
                    start = 0.85f,
                    stop = 1f,
                    fraction = 1f - pageOffset.coerceIn(0f, 1f)
                ).also { scale ->
                    scaleX = scale
                    scaleY = scale
                }

                translationX = (pagerSize?.width ?: 0f) * (pageOffset.coerceIn(0f, 1f))
            }

            // animate alpha the same way each time
            alpha = lerp(
                start = 0.0f,
                stop = 1f,
                fraction = 1f - pageOffset.absoluteValue.coerceIn(0f, 1f)
            )
        }

        Box(
            modifier = animationModifier
                .fillMaxSize()
        ) {
            CompositionLocalProvider(
                LocalNavCoordinator provides navCoordinator
            ) {
                content(pages[currentPage])
            }
        }
    }
}
