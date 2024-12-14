package com.lift.bro.presentation.lift

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.lift.EditLiftScreen
import com.lift.bro.presentation.set.EditSetVoyagerScreen
import com.lift.bro.presentation.variation.EditVariationScreen
import com.lift.bro.presentation.voyager.EditLiftVoyagerScreen
import com.lift.bro.presentation.voyager.EditVariationVoyagerScreen
import com.lift.bro.presentation.voyager.LiftDetailsVoyagerScreen

class LiftListVoyagerScreen(
): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        LiftListScreen(
            addLiftClicked = {
                navigator.push(EditLiftVoyagerScreen())
            },
            liftClicked = {
                navigator.push(LiftDetailsVoyagerScreen(liftId = it.id))
            },
            addSetClicked = {
                navigator.push(EditSetVoyagerScreen())
            },
            setClicked = {
                navigator.push(EditSetVoyagerScreen(it.id))
            }
        )
    }
}