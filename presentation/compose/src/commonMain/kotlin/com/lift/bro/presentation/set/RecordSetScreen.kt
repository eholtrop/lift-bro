package com.lift.bro.presentation.set

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import com.lift.bro.presentation.timer.TimerOverlay
import com.lift.bro.presentation.timer.TimerState

@Composable
fun RecordSetScreen() {
    Box {
        TimerOverlay(
            state = TimerState.Plan(),
            onEvent = {}
        )
    }
}
