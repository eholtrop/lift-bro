@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.lift.bro.ui.navigation.LocalNavCoordinator
import com.lift.bro.ui.navigation.LocalSnackbarHostState
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.toolbar_back_button_content_description
import org.jetbrains.compose.resources.stringResource

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
    title: String,
    snackbarHostState: SnackbarHostState = LocalSnackbarHostState.current,
    fabProperties: FabProperties? = null,
    leadingContent: @Composable () -> Unit = {
        val navCoordinator = LocalNavCoordinator.current
        TopBarIconButton(
            imageVector = Icons.Default.ArrowBack,
            contentDescription = stringResource(Res.string.toolbar_back_button_content_description),
            onClick = { navCoordinator.onBackPressed() },
        )

    },
    trailingContent: @Composable () -> Unit = {},
    topAppBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection)
            .imePadding(),
        topBar = {
            TopBar(
                title = title,
                scrollBehavior = topAppBarScrollBehavior,
                trailingContent = trailingContent,
                leadingContent = leadingContent,
            )
        },
        floatingActionButton = {
            if (fabProperties != null) {
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
        },
        floatingActionButtonPosition = FabPosition.Center,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) {
        content(it)
    }
}