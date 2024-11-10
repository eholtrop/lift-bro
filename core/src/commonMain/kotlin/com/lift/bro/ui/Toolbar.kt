package com.lift.bro.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
//import androidx.compose.material3.PlainTooltipBox
//import androidx.compose.material3.PlainTooltipState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.StoreManager
import com.lift.bro.presentation.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackButton: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
) {
    LargeTopAppBar(
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        title = {
            Text(
                modifier = modifier.semantics { heading() }.fillMaxWidth(),
                textAlign = if ((scrollBehavior?.state?.collapsedFraction ?: 0f) < .66f) TextAlign.Center else TextAlign.Start,
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.headlineLarge,
            )
        },
        navigationIcon = {
            if (showBackButton) {
                val navigator = LocalNavigator.currentOrThrow
                TopBarIconButton(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    onClick = { navigator.pop() },
                )
            }
        },
        actions = {
            trailingContent()

            val updateAvailable by StoreManager.isUpdateAvailable().collectAsState(false)

//            val tooltipState = remember { PlainTooltipState() }

            AnimatedVisibility(updateAvailable) {
//                PlainTooltipBox(
//                    tooltip = {
//                        Text("Update Available!")
//                    },
//                    tooltipState = tooltipState,
//                ) {
//                    TopBarIconButton(
//                        imageVector = Icons.Default.PlayArrow,
//                        contentDescription = "Calculator",
//                        onClick = {
//                            StoreManager.startUpdateFlow()
//                        }
//                    )
//
//                    LaunchedEffect("show tooltip") {
//                        tooltipState.show()
//                    }
//                }

            }

            TopBarIconButton(
                painter = Images.calculator(),
                contentDescription = "Calculator",
                onClick = {
                    dependencies.launchCalculator()
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
            top = MaterialTheme.spacing.one,
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
            top = MaterialTheme.spacing.one,
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