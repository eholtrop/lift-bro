package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.home.HomeScreen

class HomeVoyagerScreen(): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        HomeScreen(
            addLiftClicked = { navigator.push(EditLiftVoyagerScreen()) },
            liftClicked = { navigator.push(LiftDetailsVoyagerScreen(it.id)) }
        )
    }
}