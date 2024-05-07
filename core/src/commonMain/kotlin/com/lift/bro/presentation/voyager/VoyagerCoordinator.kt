package com.lift.bro.presentation.voyager

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import com.lift.bro.presentation.Coordinator

class VoyagerCoordinator : Coordinator {

    @Composable
    override fun render() {
        Navigator(
            screens = listOf(DashboardVoyagerScreen())
        ) {
            SlideTransition(it)
        }
    }
}
