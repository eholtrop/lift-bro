@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import com.lift.bro.ui.navigation.LocalSnackbarHostState
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.toolbar_back_button_content_description
import org.jetbrains.compose.resources.stringResource
import tv.dpal.swipenavhost.LocalNavCoordinator
import tv.dpal.swipenavhost.NavCoordinator
import kotlin.math.absoluteValue

data class FabProperties(
    val fabIcon: ImageVector,
    val contentDescription: String,
    val fabClicked: (() -> Unit)? = null,
    val preFab: @Composable (() -> Unit)? = null,
    val postFab: @Composable (() -> Unit)? = null,
    val fabEnabled: Boolean = true,
)

@Composable
fun LiftingScaffold(
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    fabProperties: FabProperties? = null,
    leadingContent: @Composable () -> Unit = { LeadingNavigationButton() },
    trailingContent: @Composable () -> Unit = {},
    tallMode: Boolean = currentWindowAdaptiveInfo()
        .windowSizeClass
        .isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND),
    topAppBarScrollBehavior: TopAppBarScrollBehavior = when (tallMode) {
        true -> TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
        false -> TopAppBarDefaults.enterAlwaysScrollBehavior()
    },
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding(),
        topBar = {
            var currentOffset by remember { mutableStateOf(topAppBarScrollBehavior.state.contentOffset) }
            var visible by remember { mutableStateOf(true) }

            if (!tallMode) {
                LaunchedEffect(topAppBarScrollBehavior.state.contentOffset) {
                    if (topAppBarScrollBehavior.state.contentOffset.absoluteValue > TopAppBarDefaults.LargeAppBarCollapsedHeight.value) {
                        visible = topAppBarScrollBehavior.state.contentOffset >= currentOffset
                        currentOffset = topAppBarScrollBehavior.state.contentOffset
                    }
                }
            }
            val animatedToolbarHeight by animateDpAsState(
                if (visible) TopAppBarDefaults.LargeAppBarCollapsedHeight else 0.dp
            )

            TopBar(
                title = title,
                description = description,
                scrollBehavior = topAppBarScrollBehavior,
                trailingContent = trailingContent,
                leadingContent = leadingContent,
                collapsedHeight = animatedToolbarHeight
            )
        },
        floatingActionButton = {
            if (fabProperties != null) {
                var currentOffset by remember { mutableStateOf(topAppBarScrollBehavior.state.contentOffset) }
                var visible by remember { mutableStateOf(true) }

                LaunchedEffect(topAppBarScrollBehavior.state.contentOffset) {
                    visible = topAppBarScrollBehavior.state.contentOffset >= currentOffset
                    currentOffset = topAppBarScrollBehavior.state.contentOffset
                }

                AnimatedVisibility(
                    visible = visible,
                    enter = fadeIn() + slideInVertically { it },
                    exit = fadeOut() + slideOutVertically { it }
                ) {
                    Row {
                        fabProperties.preFab?.invoke()
                        Space(MaterialTheme.spacing.half)
                        Button(
                            modifier = Modifier.defaultMinSize(52.dp, 52.dp),
                            enabled = fabProperties.fabEnabled,
                            onClick = { fabProperties.fabClicked?.invoke() },
                            shape = when {
                                fabProperties.preFab != null || fabProperties.postFab != null -> {
                                    RoundedCornerShape(
                                        topStartPercent = if (fabProperties.preFab != null) 25 else 50,
                                        bottomStartPercent = if (fabProperties.preFab != null) 25 else 50,
                                        topEndPercent = if (fabProperties.postFab != null) 25 else 50,
                                        bottomEndPercent = if (fabProperties.postFab != null) 25 else 50,
                                    )
                                }

                                else -> ButtonDefaults.shape
                            }
                        ) {
                            Icon(
                                imageVector = fabProperties.fabIcon,
                                contentDescription = fabProperties.contentDescription,
                            )
                        }
                        Space(MaterialTheme.spacing.half)
                        fabProperties.postFab?.invoke()
                    }
                }
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        content(it)
    }
}

@Composable
private fun LeadingNavigationButton(
    modifier: Modifier = Modifier,
    tabletMode: Boolean = currentWindowAdaptiveInfo().windowSizeClass.isWidthAtLeastBreakpoint(
        WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND
    ),
    navCoordinator: NavCoordinator = LocalNavCoordinator.current
) {
    when {
        !tabletMode || (tabletMode && navCoordinator.currentPageIndex > 1) -> {
            TopBarIconButton(
                modifier = modifier,
                imageVector = Icons.Default.ArrowBack,
                contentDescription = stringResource(Res.string.toolbar_back_button_content_description),
                onClick = { navCoordinator.onBackPressed() },
            )
        }
        tabletMode && navCoordinator.currentPageIndex == 1 -> {
            TopBarIconButton(
                modifier = modifier,
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                onClick = { navCoordinator.popToRoot(keepStack = false) },
            )
        }
    }
}
