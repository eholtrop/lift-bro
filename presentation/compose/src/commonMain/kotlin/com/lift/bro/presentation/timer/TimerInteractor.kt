package com.lift.bro.presentation.timer

import androidx.compose.runtime.Composable
import com.lift.bro.audio.AudioPlayer
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Tempo
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.presentation.timer.TimerState.*
import com.lift.bro.presentation.timer.TimerState.Plan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
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
fun rememberTimerInteractor(
    setId: String,
    setRepository: ISetRepository = dependencies.setRepository,
    uiEffects: SideEffect<TimerState, TimerEvent> = { },
): TimerInteractor = rememberInteractor(
    initialState = Plan(),
    source = {
        setRepository.listen(setId)
            .filterNotNull()
            .map { set ->
                Plan(
                    set = set,
                )
            }
    },
    reducers = timerReducers(),
    sideEffects = listOf(
        SideEffect { _, state, _ ->
            if (state is TimerState.Plan) {
                if (state.set != null) {
                    setRepository.save(state.set)
                }
            }
        },
        uiEffects,
        timerSideEffects(),
    )
)

@Composable
fun rememberTimerInteractor(
    reps: Int,
    tempo: Tempo,
    uiEffects: SideEffect<TimerState, TimerEvent> = { _, s, e -> },
): TimerInteractor = rememberInteractor(
    initialState = Plan(
        tempo = (0 until reps).map { tempo.copy() }
    ),
    reducers = timerReducers() + Reducer { state, event ->
        if (event is TimerEvent.ToggleAudio) {
            when (state) {
                is Ended -> state
                is Plan ->
                    state.copy(audio = !state.audio)

                is Running ->
                    state.copy(audio = !state.audio)
            }
        } else {
            state
        }

    },
    sideEffects = listOf(timerSideEffects(), uiEffects)
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
sealed class TimerState(
) {
    @Serializable
    data class Plan(
        val startupTime: Long = 1,
        val perSetRest: Long = 1,
        val set: LBSet? = null,
        val tempo: List<Tempo> = set?.let { (0 until it.reps).map { set.tempo } } ?: listOf(Tempo()),
        val audio: Boolean = true,
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
        val set: LBSet? = null,
        val audio: Boolean = true,
    ): TimerState() {
        val totalTime = timers.sumOf { it.totalTime }

        val currentTimer = timers.firstOrNull { it.elapsedTime < it.totalTime }
    }

    @Serializable
    data class Ended(
        val timers: List<TimerSegment>,
        val recording: String? = null,
        val set: LBSet? = null,
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

    object ToggleAudio: TimerEvent


    sealed interface Plan: TimerEvent {
        data class StartupTimeChanged(val value: Long): Plan
        data class TempoChanged(val rep: Int?, val tempo: Tempo): Plan
        data class PerSetRestChanged(val value: Long): Plan
        data object AddTimer: Plan
        data class RemoveTimer(val index: Int = 0): Plan
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

private fun timerReducers() = listOf(
    planningTimerReducer(),
    runningTimerReducer(),
    // end timer reducer
    Reducer { state, event ->
        if (state !is TimerState.Ended && event !is TimerEvent.Ended) return@Reducer state

        return@Reducer if (state is TimerState.Ended) {
            when (event) {
                TimerEvent.Ended.Restart -> Plan(
                    tempo = state.timers.drop(1).chunked(4).map {
                        Tempo(
                            down = it[0].totalTime / 1000,
                            hold = it[1].totalTime / 1000,
                            up = it[2].totalTime / 1000,
                        )
                    }
                )

                else -> state
            }
        } else {
            state
        }
    }
)

private fun planningTimerReducer(): Reducer<TimerState, TimerEvent> = Reducer { state, event ->
    if (state !is TimerState.Plan || event !is TimerEvent.Plan) return@Reducer state
    when (event) {
        is TimerEvent.Plan.PerSetRestChanged -> state.copy(perSetRest = event.value)
        is TimerEvent.Plan.StartupTimeChanged -> state.copy(startupTime = event.value)
        is TimerEvent.Plan.TempoChanged -> if (event.rep != null) {
            state.copy(
                tempo = state.tempo.mapIndexed { index, tempo ->
                    if (index == event.rep) event.tempo else tempo
                },
            )
        } else {
            state.copy(
                tempo = state.tempo.map { event.tempo.copy() },
            )
        }.copy(
            set = if (event.rep != 0) state.set else state.set?.copy(tempo = event.tempo)
        )

        TimerEvent.Plan.Start -> state.runningTimer(beep = true)

        TimerEvent.Plan.AddTimer -> state.copy(
            tempo = state.tempo + state.tempo.last().copy(),
            set = state.set?.copy(reps = state.set.reps + 1)
        )

        is TimerEvent.Plan.RemoveTimer -> state.copy(
            tempo = state.tempo.filterIndexed { index, _ -> index != event.index },
            set = state.set?.copy(reps = state.set.reps - 1)
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

        TimerEvent.Running.End -> {
            Ended(
                timers = state.timers,
                set = state.set,
            )
        }
    }
}

private fun timerSideEffects(
    audioPlayer: AudioPlayer = dependencies.audioPlayer,
): SideEffect<TimerState, TimerEvent> = SideEffect { disp, state, event ->
    when (event) {
        TimerEvent.Plan.Start, TimerEvent.Running.Resume, TimerEvent.Running.Tick -> {
            if (state is TimerState.Running) {
                if (state.elapsedTime < state.totalTime) {
                    withContext(Dispatchers.Default) {
                        delay(50)
                        disp(TimerEvent.Running.Tick)
                    }
                } else {
                    disp(TimerEvent.Running.End)
                }
            }
        }

        else -> {}
    }

    if (state is TimerState.Running && state.beep && state.audio) {
        val timer = state.currentTimer

        timer?.let {
            when {
                timer.elapsedTime > 1000 -> audioPlayer.speak(
                    ((1000 + timer.totalTime - timer.elapsedTime) / 1000).toString()
                )

                else -> audioPlayer.speak(timer.speak)
            }
        }
    }
}

private fun TimerState.Plan.runningTimer(beep: Boolean = false): TimerState.Running = TimerState.Running(
    elapsedTime = 0L,
    paused = beep,
    beep = beep,
    set = set,
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
