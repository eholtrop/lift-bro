package com.lift.bro.ui.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.home.darkIconRes
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import org.jetbrains.compose.resources.painterResource

@Composable
fun InfoDialogButton(
    modifier: Modifier = Modifier,
    dialogTitle: @Composable () -> Unit,
    dialogMessage: @Composable () -> Unit,
    buttonTint: Color = LocalContentColor.current,
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        InfoDialog(
            title = dialogTitle,
            message = dialogMessage,
            onDismissRequest = { showInfoDialog = false }
        )
    }

    IconButton(
        modifier = modifier,
        onClick = { showInfoDialog = true },
        enabled = true,
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = buttonTint,
        )
    }
}

@Composable
fun InfoDialog(
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
) {
    Dialog(
        onDismissRequest = onDismissRequest,
    ) {
        InfoSpeechBubble(
            title = title,
            message = message,
            forceDarkIcon = true
        )
    }
}

@Composable
fun InfoSpeechBubble(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    forceDarkIcon: Boolean = false,
) {
    val speechBubbleColor = MaterialTheme.colorScheme.primary
    Column {
        Column(
            modifier = modifier
                .semantics(
                    mergeDescendants = true,
                ) {
                    liveRegion = LiveRegionMode.Assertive
                }
                .background(
                    speechBubbleColor,
                    shape = MaterialTheme.shapes.medium
                )
                .padding(MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.headlineLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary
            ) {
                title()
            }

            Space(MaterialTheme.spacing.half)
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyLarge,
                LocalContentColor provides MaterialTheme.colorScheme.onPrimary
            ) {
                message()
            }
        }
        Row {
            Image(
                modifier = Modifier
                    .padding(top = MaterialTheme.spacing.quarter)
                    .size(72.dp),
                painter = painterResource(
                    if (forceDarkIcon) LocalLiftBro.current.darkIconRes() else LocalLiftBro.current.iconRes()
                ),
                contentDescription = ""
            )
            Canvas(
                modifier = Modifier.size(72.dp.div(2))
            ) {
                drawPath(
                    Path().apply {
                        moveTo(20f, 0f)
                        lineTo(0f, 60f)
                        lineTo(60f, 0f)
                        lineTo(0f, 0f)
                        close()
                    },
                    speechBubbleColor
                )
            }
        }
    }
}
