package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.lift.LiftDetailsScreen
import com.lift.bro.presentation.set.EditSetVoyagerScreen

class LiftDetailsVoyagerScreen(
    private val liftId: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LiftDetailsScreen(
            liftId = liftId,
            addVariationClicked = { navigator.push(EditVariationVoyagerScreen(parentLiftId = liftId)) },
            variationClicked = { navigator.push(VariationDetailsVoyagerScreen(variationId = it)) },
            editLiftClicked = { navigator.push(EditLiftVoyagerScreen(liftId)) },
            addSetClicked = { navigator.push(EditSetVoyagerScreen()) },
            onSetClicked = { navigator.push(EditSetVoyagerScreen(setId = it.id))}
        )
    }
}