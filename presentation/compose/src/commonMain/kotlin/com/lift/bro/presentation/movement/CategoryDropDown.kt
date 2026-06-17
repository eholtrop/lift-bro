package com.lift.bro.presentation.movement

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.domain.models.Category
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.category_drop_down_expand_menu_content_description
import lift_bro.core.generated.resources.category_drop_down_select_category_text
import org.jetbrains.compose.resources.stringResource

@Composable
fun CategoryDropDown(
    modifier: Modifier = Modifier,
    selectedCategory: Category?,
    categoryChanged: (Category) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.clickable(
            enabled = true,
            onClick = { expanded = true },
            role = Role.DropdownList,
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selectedCategory?.name ?: stringResource(Res.string.category_drop_down_select_category_text),
            style = MaterialTheme.typography.labelSmall,
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = stringResource(Res.string.category_drop_down_expand_menu_content_description)
        )
    }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = {
            expanded = false
        }
    ) {
        val lifts by dependencies.liftRepository.listenAll().collectAsState(emptyList())

        lifts.forEach { lift ->
            DropdownMenuItem(
                text = { Text(lift.name) },
                onClick = {
                    categoryChanged(lift)
                    expanded = false
                }
            )
        }
    }
}
