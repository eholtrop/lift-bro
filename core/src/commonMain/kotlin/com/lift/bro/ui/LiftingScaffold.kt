package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.lift.bro.presentation.spacing

@Composable
fun LiftingScaffold(
    fabIcon: ImageVector,
    contentDescription: String,
    fabClicked: (() -> Unit)? = null,
    preFab: @Composable (() -> Unit)? = null,
    postFab: @Composable (() -> Unit)? = null,
    fabEnabled: Boolean = true,
    topBar: @Composable () -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = topBar,
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