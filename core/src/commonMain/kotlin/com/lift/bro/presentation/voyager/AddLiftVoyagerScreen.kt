package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.lift.AddLiftScreen

class AddLiftVoyagerScreen(): Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        AddLiftScreen(
            liftSaved = {
                navigator.pop()
            }
        )
    }
}