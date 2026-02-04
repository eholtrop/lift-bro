package com.lift.bro.presentation.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.timer.TimerState.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.until
import kotlinx.serialization.Serializable
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import kotlin.math.max

typealias TimerInteractor = Interactor<TimerState, TimerEvent>

@Composable
fun rememberTimerInteractor(): TimerInteractor = rememberInteractor(
    initialState = TimerState.Plan(),
    reducers = listOf(
        planningTimerReducer() as Reducer<TimerState, TimerEvent>,
        runningTimerReducer() as Reducer<TimerState, TimerEvent>,
    ),
    sideEffects = listOf(runningSideEffects())
)

@Serializable
sealed class TimerState {
    @Serializable
    data class Plan(
        val startupTime: Long = 10,
        val tempo: List<Tempo> = listOf(Tempo(), Tempo(), Tempo(),),
        val perSetRest: Long = 3,
    ): TimerState()

    @Serializable
    data class Running(
        val elapsedTime: Long,
        val timers: List<TimerSegment>,
        val paused: Boolean,
        val lastTickTime: Instant = Clock.System.now(),
    ): TimerState() {
        val elapsedTimeInSeconds = elapsedTime / 1000f

        val  totalTime = timers.sumOf { it.totalTime }

        val currentTimer = timers.firstOrNull { it.elapsedTime < it.totalTime }
    }

    @Serializable
    data class Ended(
        val elapsedTime: Long,
        val timers: List<TimerSegment>,
    ): TimerState()
}

@Serializable
data class TimerSegment(
    val name: String,
    val elapsedTime: Long,
    val totalTime: Long,
) {
    val progress = mutableStateOf(elapsedTime.toFloat() / max(totalTime, 1))
}

sealed interface TimerEvent {
    sealed interface Plan: TimerEvent {
        data class StartupTimeChanged(val value: Long): Plan
        data class TempoChanged(val rep: Int, val tempo: Tempo): Plan
        data class PerSetRestChanged(val value: Long): Plan
        object Start: Plan
    }

    sealed interface Running: TimerEvent {
        object Pause: Running
        object Resume: Running
        object Stop: Running
        object End: Running
        object Tick: Running
    }

    sealed interface Ended: TimerEvent {
        object Restart: Ended
    }
}

private fun planningTimerReducer(): Reducer<TimerState, TimerEvent> = Reducer { state, event ->
    if (state !is TimerState.Plan || event !is TimerEvent.Plan) return@Reducer state
    when (event) {
        is TimerEvent.Plan.PerSetRestChanged -> state.copy(perSetRest = event.value)
        is TimerEvent.Plan.StartupTimeChanged -> state.copy(startupTime = event.value)
        is TimerEvent.Plan.TempoChanged -> state.copy(
            tempo = state.tempo.mapIndexed { index, tempo ->
                if (index == event.rep) event.tempo else tempo
            }
        )

        TimerEvent.Plan.Start -> TimerState.Running(
            elapsedTime = 0L,
            timers = listOf(
                TimerSegment(
                    name = "Setup",
                    totalTime = state.startupTime * 1000L,
                    elapsedTime = 0
                )
            ) + state.tempo.map {
                listOf(
                    TimerSegment(
                        name = "Ecc (Down)",
                        elapsedTime = 0,
                        totalTime = (it.down) * 1000L,
                    ),
                    TimerSegment(
                        name = "Iso (Hold)",
                        elapsedTime = 0,
                        totalTime = (it.hold) * 1000L,
                    ),
                    TimerSegment(
                        name = "Con (Up)",
                        elapsedTime = 0,
                        totalTime = (it.up) * 1000L,
                    ),
                    TimerSegment(
                        name = "Rest",
                        totalTime = state.perSetRest * 1000L,
                        elapsedTime = 0
                    )
                )
            }.flatten(),
            paused = false,
            lastTickTime = Clock.System.now(),
        )
    }
}

private fun runningTimerReducer(): Reducer<TimerState, TimerEvent> = Reducer { state, event ->
    if (state !is TimerState.Running || event !is TimerEvent.Running) return@Reducer state
    when (event) {
        TimerEvent.Running.Pause -> state.copy(paused = true)
        TimerEvent.Running.Resume -> state.copy(
            paused = false,
            lastTickTime = Clock.System.now()
        )

        TimerEvent.Running.Stop -> Plan()
        TimerEvent.Running.Tick -> {
            if (state.paused) return@Reducer state

            val now = Clock.System.now()
            val elapsedTime = state.elapsedTime + state.lastTickTime.until(now, DateTimeUnit.MILLISECOND)
//            if (elapsedTime >= state.timers.sumOf { it.totalTime }) {
//                return@Reducer Ended(
//                    elapsedTime = elapsedTime,
//                    timers = state.timers,
//                )
//            } else {
            var timerStart = 0L
            state.copy(
                elapsedTime = elapsedTime,
                timers = state.timers.map { timer ->
                    timer.copy(
                        elapsedTime = when {
                            elapsedTime >= timerStart -> minOf(elapsedTime - timerStart, timer.totalTime)
                            else -> 0L
                        },
                    ).also {
                        timerStart += timer.totalTime.toInt()
                    }
                },
                lastTickTime = now,
            )
//            }
        }

//        TimerEvent.Running.End -> TimerState.Ended(
//            elapsedTime = state.elapsedTime,
//            timers = state.timers,
//        )
        TimerEvent.Running.End -> state//TODO()
    }
}

private fun runningSideEffects(): SideEffect<TimerState, TimerEvent> = SideEffect { disp, state, event ->
    when (event) {
        TimerEvent.Plan.Start, TimerEvent.Running.Resume, TimerEvent.Running.Tick -> {
            withContext(Dispatchers.Default) {
                delay(10)
                disp(TimerEvent.Running.Tick)
            }
        }

        else -> {}
    }
}
