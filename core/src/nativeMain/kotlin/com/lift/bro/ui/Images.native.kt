package com.lift.bro.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.ic_calculator
import lift_bro.core.generated.resources.ic_calendar
import lift_bro.core.generated.resources.ic_dumbbell
import lift_bro.core.generated.resources.view_dashboard
import org.jetbrains.compose.resources.painterResource

actual object Images {
    @Composable
    actual fun calculator(): Painter {
        return painterResource(Res.drawable.ic_calculator)
    }

    @Composable
    actual fun addSet(): Painter {
        return painterResource(Res.drawable.ic_dumbbell)
    }

    @Composable
    actual fun dashboardMenuIcon(): Painter {
        return painterResource(Res.drawable.view_dashboard)
    }

    @Composable
    actual fun calendarMenuIcon(): Painter {
        return painterResource(Res.drawable.ic_calendar)
    }

}