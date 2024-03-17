package com.lift.bro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter

expect object Images {

    @Composable
    fun calculator(): Painter

    @Composable
    fun addSet(): Painter

    @Composable
    fun dashboardMenuIcon(): Painter

    @Composable
    fun calendarMenuIcon(): Painter
}