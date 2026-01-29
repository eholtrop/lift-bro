@file:OptIn(ExperimentalMaterial3Api::class)

package tv.dpal.swipenavhost

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.absoluteValue

@Serializable
open class Destination

val LocalNavCoordinator = compositionLocalOf<NavCoordinator> {
    error("NavHostController was not set")
}

@Composable
inline fun <reified T: Destination> SwipeableNavHost(
    modifier: Modifier = Modifier,
    navCoordinator: NavCoordinator,
    crossinline key: (T) -> Any = { Json.encodeToString(it).hashCode() },
    crossinline content: @Composable (T) -> Unit,
) {
    val tabletMode = currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    )
    val pages by navCoordinator.pagesAsFlow.collectAsState(emptyList())
    val currentPage by navCoordinator.currentPageAsFlow.collectAsState(null)

    val pagerPages = pages.let { ps ->
        when {
            ps.isEmpty() -> ps
            tabletMode -> ps.drop(1)
            else -> ps
        }
    }

    val homePage = if (tabletMode) pages.firstOrNull() else null

    val savedPagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { pagerPages.size }
    )

    LaunchedEffect(currentPage) {
        if (tabletMode && savedPagerState.currentPage > 0) {
            savedPagerState.animateScrollToPage(pages.indexOf(currentPage) - 1)
        } else {
            savedPagerState.animateScrollToPage(pages.indexOf(currentPage))
        }
    }
    val keyboard = LocalSoftwareKeyboardController.current
    LaunchedEffect(savedPagerState.currentPage) {
        if (tabletMode && savedPagerState.currentPage > 0) {
            navCoordinator.updateCurrentIndex(savedPagerState.currentPage + 1)
        } else {
            navCoordinator.updateCurrentIndex(savedPagerState.currentPage)
        }
        keyboard?.hide()
    }

    CompositionLocalProvider(
        LocalNavCoordinator provides navCoordinator
    ) {
        Row(
            modifier = modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
        ) {
            homePage?.let {
                Box(
                    modifier = Modifier.fillMaxWidth(if (pagerPages.isEmpty()) .666f else .5f)
                        .fillMaxHeight()
                        .animateContentSize()
                ) {
                    content(it as T)
                }
            }
            AnimatedVisibility(
                visible = pagerPages.isNotEmpty(),
                enter = fadeIn() + slideInHorizontally { it },
                exit = fadeOut()
            ) {
                var pagerSize: Size? by remember { mutableStateOf(null) }
                Box {
                    HorizontalPager(
                        modifier = Modifier.graphicsLayer {
                            pagerSize = this.size
                        },
                        key = {
                            key(pagerPages[it.hashCode()] as T)
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
                            content(pagerPages[currentPage] as T)
                        }
                    }
                    if (pagerPages.size > 1) {
                        Row(
                            modifier = Modifier.align(Alignment.TopCenter)
                                .statusBarsPadding()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            pagerPages.forEachIndexed { index, _ ->
                                Surface(
                                    modifier = Modifier.height(8.dp).aspectRatio(1f),
                                    color = if (index == savedPagerState.currentPage) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    },
                                    shape = MaterialTheme.shapes.extraSmall
                                ) {
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
