package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.variation.VariationDetailsScreen

class VariationDetailsVoyagerScreen(
    private val variationId: String
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        VariationDetailsScreen(
            variationId = variationId,
            addSetClicked = { },
            editClicked = { navigator.push(EditVariationVoyagerScreen(variationId = variationId)) }
        )
    }
}