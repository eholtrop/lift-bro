package com.lift.bro.ui.dialog

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusEvent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import com.lift.bro.presentation.LocalLiftBro
import com.lift.bro.presentation.home.darkIconRes
import com.lift.bro.presentation.home.iconRes
import com.lift.bro.ui.Space
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.resources.painterResource
import tv.dpal.logging.Log
import tv.dpal.logging.d

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
fun InfoDialogButton(
    modifier: Modifier = Modifier,
    dialogTitle: @Composable () -> Unit,
    dialogMessage: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    var showInfoDialog by remember { mutableStateOf(false) }

    if (showInfoDialog) {
        InfoDialog(
            title = dialogTitle,
            message = dialogMessage,
            onDismissRequest = { showInfoDialog = false }
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(
            modifier = modifier,
            onClick = { showInfoDialog = true },
            enabled = true,
            contentPadding = PaddingValues(0.dp),
            colors = ButtonDefaults.textButtonColors(),
        ) {
            content()
        }
        Icon(
            modifier = Modifier.size(11.dp),
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
        )
    }
}

@Composable
fun InfoDialog(
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    onDismissRequest: () -> Unit,
    properties: DialogProperties = DialogProperties(),
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = properties,
    ) {
        InfoSpeechBubble(
            title = title,
            message = message,
            forceDarkIcon = true,
            onConfirmClicked = onDismissRequest
        )
    }
}

@Composable
fun InfoSpeechBubble(
    modifier: Modifier = Modifier,
    title: @Composable () -> Unit,
    message: @Composable () -> Unit,
    forceDarkIcon: Boolean = false,
    onConfirmClicked: (() -> Unit)? = null,
) {
    val speechBubbleColor = MaterialTheme.colorScheme.primary

    SubcomposeLayout(
        modifier = Modifier.zIndex(Float.MAX_VALUE)
    ) { constraints ->
        val main = subcompose(0) {
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
        }.map { it.measure(constraints = constraints) }

        val maxSize = main.fold(IntSize.Zero) { currentMax, mainPlaceable ->
            IntSize(
                width = maxOf(currentMax.width, mainPlaceable.width),
                height = maxOf(currentMax.height, mainPlaceable.height)
            )
        }

        val dependants = subcompose(1) {
            Row(
            ) {
                Row {
                    Image(
                        modifier = Modifier
                            .clickable { onConfirmClicked?.invoke() }
                            .padding(top = MaterialTheme.spacing.quarter)
                            .defaultMinSize(72.dp, 72.dp)
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
        }.map { it.measure(constraints) }

        val confirm = onConfirmClicked?.let {
            subcompose(2) {
                onConfirmClicked?.let {
                    Button(
                        onClick = onConfirmClicked,
                        shape = CircleShape,
                        colors = ButtonDefaults.textButtonColors(),
                    ) {
                        Text(
                            text = "Okay",
                            style = MaterialTheme.typography.displaySmall,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }.map { it.measure(constraints = constraints) }
        }
        val confirmWidth = confirm?.fold(0) { width, placeable -> width + placeable.width } ?: 0

        layout(maxSize.width, maxSize.height + dependants.fold(0) { height, placeable -> height + placeable.height }) {
            main.forEach { it.placeRelative(0, 0) }
            dependants.forEach {
                it.placeRelative(0, main.firstOrNull()?.height ?: 0, Float.MAX_VALUE)
            }
            confirm?.first()?.placeRelative(
                x = main.first().width - confirm.first().width,
                y = main.first().height,
            )
        }
    }
}

@Preview
@Composable
fun InfoDialogButtonPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Row(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            InfoDialogButton(
                dialogTitle = { },
                dialogMessage = {
                }
            )

            InfoDialogButton(
                dialogTitle = { },
                dialogMessage = { },
                buttonTint = MaterialTheme.colorScheme.error
            )

            InfoDialogButton(
                dialogTitle = { },
                dialogMessage = { },
            ) {
                Text("Test")
            }
        }
    }
}

@Preview
@Composable
fun InfoSpeechBubblePreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        Column(
            modifier = Modifier.padding(MaterialTheme.spacing.one),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(MaterialTheme.spacing.one)
        ) {
            InfoSpeechBubble(
                title = { Text("Welcome!") },
                message = {
                    Column {
                        Text("This is an informational speech bubble.")
                        Space(MaterialTheme.spacing.half)
                        Text("It can contain multiple lines of text and complex layouts.")
                    }
                },
                forceDarkIcon = false,
                onConfirmClicked = {},
            )

            InfoSpeechBubble(
                title = { Text("Pro Tip") },
                message = { Text("Always warm up before lifting heavy weights!") },
                forceDarkIcon = true,
                onConfirmClicked = {},
            )

            InfoSpeechBubble(
                title = { Text("Pro Tip") },
                message = { Text("Alwaysdo something") },
                forceDarkIcon = true,
                onConfirmClicked = {},
            )
        }
    }
}
