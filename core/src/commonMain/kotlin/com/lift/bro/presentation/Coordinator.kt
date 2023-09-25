package com.lift.bro.presentation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.lift.bro.presentation.home.HomeScreen
import com.lift.bro.presentation.lift.AddLiftScreen

interface Coordinator {
    @Composable
    fun render()
}

