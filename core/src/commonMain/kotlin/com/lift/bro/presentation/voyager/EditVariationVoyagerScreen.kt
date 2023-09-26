package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.variation.EditVariationScreen

class EditVariationVoyagerScreen(
    private val variationId: String? = null,
    private val parentLiftId: String? = null,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        EditVariationScreen(
            id = variationId,
            parentLiftId = parentLiftId,
            variationSaved = { navigator.pop() }
        )
    }
}