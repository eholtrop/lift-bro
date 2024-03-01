package com.lift.bro.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.di.dependencies
import com.lift.bro.presentation.spacing

@Composable
fun TopBar(
    modifier: Modifier = Modifier,
    title: String,
    showBackButton: Boolean = false,
    trailingContent: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier.defaultMinSize(minHeight = 128.dp)
            .fillMaxWidth()
    ) {

        if (showBackButton) {
            val navigator = LocalNavigator.currentOrThrow
            TopBarIconButton(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                onClick = { navigator.pop() },
            )
        }

        ToolbarTitle(
            modifier = Modifier.align(Alignment.BottomCenter),
            title = title,
        )

        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            content = {
                trailingContent()
                TopBarIconButton(
                    painter = Images.calculator(),
                    contentDescription = "Calculator",
                    onClick = {
                        dependencies.launchCalculator()
                    }
                )
            }
        )
    }
}

@Composable
fun ToolbarTitle(
    modifier: Modifier = Modifier,
    title: String,
) {
    Text(
        modifier = modifier,
        text = title,
        color = MaterialTheme.colorScheme.onBackground,
        style = MaterialTheme.typography.headlineLarge,
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