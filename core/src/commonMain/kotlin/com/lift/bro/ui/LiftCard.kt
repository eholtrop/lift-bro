package com.lift.bro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import comliftbrodb.Lift
import spacing

@Composable
fun LiftCard(
    modifier: Modifier = Modifier,
    lift: Lift,
    onClick: (Lift) -> Unit
) {

    Card(
        modifier = modifier
            .aspectRatio(1f),
        onClick = {  onClick(lift) }
    ) {
        if (lift.name.isNotEmpty()) {
            Text(
                text = lift.name,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}