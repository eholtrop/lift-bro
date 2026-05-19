package com.lift.bro.ui.banner

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import com.lift.bro.ui.Card
import com.lift.bro.ui.Space

@Composable
fun DashboardBanner(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    onClick: () -> Unit,
    onClickLabel: String? = null,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        backgroundBrush = Brush.linearGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.surface,
            )
        ),
        onClick = {
            onClick()
        }
    ) {
        Row {
            content()
            Space()
            IconButton(
                onClick = {
                    onDismiss()
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = onClickLabel,
                )
            }
        }
    }
}
