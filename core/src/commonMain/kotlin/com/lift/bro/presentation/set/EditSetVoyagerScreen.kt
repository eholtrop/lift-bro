package com.lift.bro.presentation.set

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.variation.EditVariationScreen
import com.lift.bro.presentation.voyager.EditLiftVoyagerScreen
import com.lift.bro.presentation.voyager.EditVariationVoyagerScreen

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
            liftId = null,
            setSaved = { navigator.pop() },
            createVariationClicked = { navigator.push(EditVariationVoyagerScreen()) },
            createLiftClicked = {navigator.push(EditLiftVoyagerScreen()) }
        )
    }
}