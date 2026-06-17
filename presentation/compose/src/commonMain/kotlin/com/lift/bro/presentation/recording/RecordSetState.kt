package com.lift.bro.presentation.recording

import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.serializers.InstantSerializer
import com.lift.bro.presentation.camera.CameraController
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
sealed class RecordSetState {
    abstract val cameraController: CameraController?
    abstract val backingSet: LBSet?

    @Serializable
    data class Planning(
        val cameraEnabled: Boolean = false,
        val metronomeEnabled: Boolean = true,
        @Transient override val cameraController: CameraController? = null,
        override val backingSet: LBSet? = null,
    ): RecordSetState()

    @Serializable
    data class Recording(
        val metronomeState: MetronomeState? = null,
        val cameraEnabled: Boolean = false,
        val videoUri: String? = null,
        @Transient override val cameraController: CameraController? = null,
        override val backingSet: LBSet? = null,
    ): RecordSetState()

    @Serializable
    data class Recorded(
        val videoUri: String? = null,
        val lbSet: LBSet? = null,
        @Transient override val cameraController: CameraController? = null,
        override val backingSet: LBSet? = null,
    ): RecordSetState()
}

@Serializable
data class MetronomeState(
    @Serializable(with = InstantSerializer::class)
    val lastBeepTime: Instant = Clock.System.now(),
)

sealed interface RecordSetEvent {

    sealed interface PlanningEvent: RecordSetEvent {
        data object ToggleMetronome: PlanningEvent
        data object ToggleCamera: PlanningEvent
        data object StartRecording: PlanningEvent
        data object CameraPermissionGranted: PlanningEvent
    }

    sealed interface RecordingEvent: RecordSetEvent {
        data object Stop: RecordingEvent
        data object Tick: RecordingEvent
        data object Beep: RecordingEvent
    }

    sealed interface RecordedEvent: RecordSetEvent {
        data object SaveToSet: RecordedEvent
        data class VideoStored(val videoUri: String): RecordedEvent
        data object ReRecordVideo: RecordedEvent
    }
}
