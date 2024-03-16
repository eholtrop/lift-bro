package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.home.DashboardScreen
import com.lift.bro.presentation.set.EditSetVoyagerScreen

class DashboardVoyagerScreen(): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        DashboardScreen(
            addLiftClicked = { navigator.push(EditLiftVoyagerScreen()) },
            liftClicked = { navigator.push(LiftDetailsVoyagerScreen(it.id)) },
            addSetClicked = { navigator.push(EditSetVoyagerScreen()) },
            setClicked = { navigator.push(EditSetVoyagerScreen(setId = it.id)) }
        )
    }
}