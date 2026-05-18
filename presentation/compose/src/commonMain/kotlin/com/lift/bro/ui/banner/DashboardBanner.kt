package com.lift.bro.ui.banner

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.Role
import com.lift.bro.ui.Card
import com.lift.bro.ui.Space
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.release_notes_row_ignore_content_description
import org.jetbrains.compose.resources.stringResource

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
                    contentDescription = stringResource(Res.string.release_notes_row_ignore_content_description)
                )
            }
        }
    }
}
