package com.lift.bro.presentation.timer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.lift.bro.audio.AudioPlayer
import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Tempo
import com.lift.bro.presentation.timer.TimerState.Plan
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
import tv.dpal.logging.Log
import tv.dpal.logging.d
import kotlin.math.max

typealias TimerInteractor = Interactor<TimerState, TimerEvent>

@Composable
fun rememberTimerInteractor(
    reps: Int,
    tempo: Tempo,
): TimerInteractor = rememberInteractor(
    initialState = Plan(
        tempo = (0..reps).map { tempo.copy() }
    ),
    reducers = listOf(
        planningTimerReducer() as Reducer<TimerState, TimerEvent>,
        runningTimerReducer() as Reducer<TimerState, TimerEvent>,
    ),
    sideEffects = listOf(runningSideEffects())
)

enum class RecordingStatus {
    Planning, Started, Paused, Resumed, Stopped,
}

data class RecordState(
    val timerState: TimerState?,
    val cameraState: CameraState?,
    val status: RecordingStatus,
)

data class CameraState(
    val value: Int,
)

@Serializable
sealed class TimerState {
    @Serializable
    data class Plan(
        val startupTime: Long = 10,
        val tempo: List<Tempo> = listOf(Tempo(), Tempo(), Tempo()),
        val perSetRest: Long = 3,
    ): TimerState() {
        val runningTimer get() = this.runningTimer()
    }

    @Serializable
    data class Running(
        val elapsedTime: Long,
        val timers: List<TimerSegment>,
        val paused: Boolean,
        val beep: Boolean,
        val lastTickTime: Instant = Clock.System.now(),
    ): TimerState() {
        val totalTime = timers.sumOf { it.totalTime }

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
    val speak: String = "",
    val elapsedTime: Long,
    val totalTime: Long,
) {
    val progress = elapsedTime.toFloat() / max(totalTime, 1)
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

        TimerEvent.Plan.Start -> state.runningTimer(beep = true)
    }
}

private fun TimerState.Plan.runningTimer(beep: Boolean = false): TimerState.Running = TimerState.Running(
    elapsedTime = 0L,
    paused = beep,
    beep = beep,
    timers = listOf(
        TimerSegment(
            name = "Setup",
            speak = "Get Ready",
            totalTime = startupTime * 1000L,
            elapsedTime = 0
        )
    ) + tempo.map {
        listOf(
            TimerSegment(
                name = "Ecc (Down)",
                speak = "Down",
                elapsedTime = 0,
                totalTime = (it.down) * 1000L,
            ),
            TimerSegment(
                name = "Iso (Hold)",
                speak = "Hold",
                elapsedTime = 0,
                totalTime = (it.hold) * 1000L,
            ),
            TimerSegment(
                name = "Con (Up)",
                speak = "Up",
                elapsedTime = 0,
                totalTime = (it.up) * 1000L,
            ),
            TimerSegment(
                name = "Rest",
                speak = "Rest",
                totalTime = perSetRest * 1000L,
                elapsedTime = 0
            )
        )
    }.flatten(),
)

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
            var timerStart = 0L
            state.copy(
                elapsedTime = elapsedTime,
                beep = (elapsedTime / 1000) != (state.elapsedTime / 1000),
                timers = state.timers.map { timer ->
                    timer.copy(
                        elapsedTime = when {
                            elapsedTime >= timerStart -> minOf(elapsedTime - timerStart, timer.totalTime)
                            else -> 0L
                        },
                    ).also {
                        timerStart += timer.totalTime
                    }
                },
                lastTickTime = now,
            )
        }

        TimerEvent.Running.End -> state//TODO()
    }
}

private fun runningSideEffects(
    audioPlayer: AudioPlayer = dependencies.audioPlayer,
): SideEffect<TimerState, TimerEvent> = SideEffect { disp, state, event ->
    when (event) {
        TimerEvent.Plan.Start, TimerEvent.Running.Resume, TimerEvent.Running.Tick -> {
            if (state is TimerState.Running && state.elapsedTime < state.totalTime) {
                withContext(Dispatchers.Default) {
                    delay(50)
                    disp(TimerEvent.Running.Tick)
                }
            }
        }

        else -> {}
    }



    if (state is TimerState.Running && state.beep) {
        val timer = state.currentTimer

        timer?.let {
            when {
                timer.elapsedTime > 1000 -> audioPlayer.speak(((1000 + timer.totalTime - timer.elapsedTime) / 1000).toString())
                else -> audioPlayer.speak(timer.speak)
            }
            Log.d(message = "Beep")
        }
    }
}
