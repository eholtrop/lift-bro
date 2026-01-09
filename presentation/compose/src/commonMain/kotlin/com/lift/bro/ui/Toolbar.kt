package com.lift.bro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import com.lift.bro.presentation.LocalCalculatorVisibility
import com.lift.bro.presentation.StoreManager
import com.lift.bro.ui.theme.spacing
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.ic_calculator
import lift_bro.core.generated.resources.toolbar_calculator_button_content_description
import lift_bro.core.generated.resources.toolbar_update_button_content_description
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
fun TopBar(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    description: @Composable (() -> Unit)? = null,
    trailingContent: @Composable () -> Unit = {},
    leadingContent: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    var toolbarHeight by remember { mutableStateOf(TopAppBarDefaults.LargeAppBarExpandedHeight) }

    LargeTopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        ),
        expandedHeight = toolbarHeight,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .wrapContentHeight(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if ((scrollBehavior?.state?.collapsedFraction ?: 0f) < .66f) Arrangement.Center else Arrangement.Start,
            ) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onBackground
                ) {
                Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        title()
                        if ((scrollBehavior?.state?.collapsedFraction ?: 0f) < .66f) {
                            CompositionLocalProvider(
                                LocalTextStyle provides MaterialTheme.typography.titleMedium,
                            ) {
                                description?.invoke()
                            }
                        }
                    }
                }
            }
        },
        navigationIcon = {
            leadingContent()
        },
        actions = {
            trailingContent()

            val updateAvailable by StoreManager.isUpdateAvailable().collectAsState(false)

            AnimatedVisibility(updateAvailable) {
                TopBarIconButton(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = stringResource(Res.string.toolbar_update_button_content_description),
                    onClick = {
                        StoreManager.startUpdateFlow()
                    }
                )
            }

            val showCalculator = LocalCalculatorVisibility.current
            TopBarIconButton(
                painter = painterResource(Res.drawable.ic_calculator),
                contentDescription = stringResource(Res.string.toolbar_calculator_button_content_description),
                onClick = {
                    showCalculator.value = true
                }
            )
        },
    )
}

@Composable
fun TopBarIconButton(
    imageVector: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.one,
        ),
        onClick = onClick,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun TopBarIconButton(
    painter: Painter,
    contentDescription: String,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.one,
        ),
        onClick = onClick,
    ) {
        Icon(
            painter = painter,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun TopBarButton(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    IconButton(
        modifier = Modifier.padding(
            start = MaterialTheme.spacing.one,
        ),
        onClick = onClick,
        content = content,
    )
}
