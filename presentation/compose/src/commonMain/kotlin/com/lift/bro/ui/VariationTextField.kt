package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.transparentColors
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.edit_lift_screen_variation_delete_cta_content_description
import lift_bro.core.generated.resources.edit_lift_screen_variation_name_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun VariationTextField(
    variation: Variation,
    liftName: String,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    focusRequester: FocusRequester = FocusRequester(),
    onNameChange: (String) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = MaterialTheme.shapes.medium,
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var name by remember { mutableStateOf(variation.name ?: "") }

        TextField(
            modifier = Modifier.weight(1f).focusRequester(focusRequester),
            value = name,
            leadingIcon = if (variation.favourite) {
                {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favourite",
                    )
                }
            } else {
                null
            },
            singleLine = true,
            onValueChange = {
                name = it
                onNameChange(it)
            },
            maxLines = 1,
            placeholder = { Text(stringResource(Res.string.edit_lift_screen_variation_name_placeholder)) },
            suffix = {
                if (liftName.isNotBlank()) {
                    Text(
                        text = if (liftName.length > 12) {
                            liftName.substring(
                                0,
                                11
                            ) + "..."
                        } else {
                            liftName
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            },
            colors = TextFieldDefaults.transparentColors()
        )
        IconButton(onClick = onDelete) {
            Icon(
                Icons.Default.Delete,
                contentDescription = stringResource(
                    Res.string.edit_lift_screen_variation_delete_cta_content_description
                )
            )
        }
    }
}

