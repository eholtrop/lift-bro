package com.lift.bro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import com.lift.bro.R

actual object Images {

    @Composable
    actual fun calculator() = painterResource(R.drawable.calculator)

    @Composable
    actual fun addSet() = painterResource(R.drawable.dumbbell)

    @Composable
    actual fun dashboardMenuIcon() = painterResource(R.drawable.view_dashboard)

    @Composable
    actual fun calendarMenuIcon() = painterResource(R.drawable.calendar_blank)
}