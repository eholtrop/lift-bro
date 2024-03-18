package com.lift.bro.presentation.set

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class EditSetVoyagerScreen(
    private val setId: String? = null,
    private val variationId: String? = null,
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        EditSetScreen(
            setId = setId,
            variationId = variationId,
            setSaved = { navigator.pop() },
        )
    }
}