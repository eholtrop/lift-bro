package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.style.TextOverflow
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.lift.transparentColors
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.flow.collectLatest
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.edit_lift_screen_variation_name_placeholder
import org.jetbrains.compose.resources.stringResource

@Composable
fun VariationTextField(
    modifier: Modifier = Modifier,
    variation: Variation,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    focusRequester: FocusRequester = FocusRequester(),
    onNameChanged: (String) -> Unit,
    onLiftChanged: ((Lift) -> Unit)? = null,
    action: @Composable (() -> Unit)? = null,
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
                onNameChanged(it)
            },
            maxLines = 1,
            placeholder = { Text(stringResource(Res.string.edit_lift_screen_variation_name_placeholder)) },
            colors = TextFieldDefaults.transparentColors()
        )
        Row {
            val liftName = variation.lift?.name
            if (onLiftChanged != null) {
                var lifts by remember { mutableStateOf(emptyList<Lift>()) }
                var showMenu by remember { mutableStateOf(false) }

                Button(
                    modifier = Modifier.defaultMinSize(minHeight = TextFieldDefaults.MinHeight),
                    onClick = { showMenu = true },
                    colors = ButtonDefaults.textButtonColors(),
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.half),
                    shape = RectangleShape,
                ) {
                    if (liftName?.isNotBlank() == true) {
                        Text(
                            text = if (liftName.length > 12) {
                                liftName.take(11) + "..."
                            } else {
                                liftName
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "Select Lift"
                        )
                    }
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    lifts.forEach { lift ->
                        DropdownMenuItem(
                            text = { Text(lift.name) },
                            onClick = {
                                onLiftChanged(lift)
                                showMenu = false
                            }
                        )
                    }
                }

                LaunchedEffect(Unit) {
                    dependencies.liftRepository.listenAll()
                        .collectLatest { lifts = it }
                }
            } else {
                liftName?.let {
                    Text(
                        text = if (liftName.length > 12) {
                            liftName.take(11) + "..."
                        } else {
                            liftName
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        action?.invoke()
    }
}
