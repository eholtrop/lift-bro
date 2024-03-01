package com.lift.bro.ui

import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.domain.models.Lift

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
                textAlign = TextAlign.Center,
            )
        }
    }
}