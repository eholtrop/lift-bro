package com.lift.bro.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.domain.models.Variation
import com.lift.bro.presentation.spacing

@Composable
fun VariationCard(
    modifier: Modifier = Modifier,
    variation: Variation,
    onClick: (Variation) -> Unit
) {
    Card(
        modifier = modifier
            .padding(MaterialTheme.spacing.half),
        onClick = {  onClick(variation) }
    ) {
        if (variation.name.isNullOrBlank().not()) {
            Text(
                text = variation.name!!,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
            )
        }
    }
}