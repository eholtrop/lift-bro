@file:OptIn(ExperimentalMaterial3Api::class)

package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import com.lift.bro.presentation.spacing

@Composable
fun LiftingScaffold(
    fabIcon: ImageVector,
    contentDescription: String,
    fabClicked: (() -> Unit)? = null,
    preFab: @Composable (() -> Unit)? = null,
    postFab: @Composable (() -> Unit)? = null,
    fabEnabled: Boolean = true,
    title: String,
    actions: @Composable RowScope.() -> Unit = {},
    topAppBarScrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
                 TopBar(
                     title = title,
                     scrollBehavior = topAppBarScrollBehavior,
                     trailingContent = actions,
                     showBackButton = LocalNavigator.current?.canPop ?: false
                 )
        },
        floatingActionButton = {
            Row {
                preFab?.invoke()
                Space(MaterialTheme.spacing.half)
                Button(
                    modifier = Modifier.defaultMinSize(52.dp, 52.dp),
                    enabled = fabEnabled,
                    onClick = { fabClicked?.invoke() },
                    shape = when {
                        preFab != null || postFab != null -> {
                            RoundedCornerShape(
                                topStartPercent = if (preFab != null) 25 else 50,
                                bottomStartPercent = if (preFab != null) 25 else 50,
                                topEndPercent = if (postFab != null) 25 else 50,
                                bottomEndPercent = if (postFab != null) 25 else 50,
                            )
                        }
                        else -> ButtonDefaults.shape
                    }
                ) {
                    Icon(
                        imageVector = fabIcon,
                        contentDescription = contentDescription,
                    )
                }
                Space(MaterialTheme.spacing.half)
                postFab?.invoke()
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
    ) {
        content(it)
    }
}