package com.lift.bro.presentation.recording

import androidx.compose.runtime.Composable
import com.benasher44.uuid.uuid4
import com.lift.bro.di.dependencies
import com.lift.bro.di.setRepository
import com.lift.bro.domain.repositories.ISetRepository
import com.lift.bro.presentation.LiftBroNavCoordinator
import com.lift.bro.presentation.LocalNavCoordinator
import com.lift.bro.presentation.camera.CameraControllerFactory
import com.lift.bro.presentation.camera.rememberCameraControllerFactory
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.div
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.until
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import kotlin.time.Clock

typealias RecordingInteractor = Interactor<RecordSetState, RecordSetEvent>

@Composable
fun rememberRecordingInteractor(
    setId: String,
    cameraControllerFactory: CameraControllerFactory = rememberCameraControllerFactory(),
    navController: LiftBroNavCoordinator = LocalNavCoordinator.current,
    setRepository: ISetRepository = dependencies.setRepository,
): RecordingInteractor = rememberInteractor(
    initialState = RecordSetState.Planning(
        cameraEnabled = true,
    ),
    source = { state ->
        setRepository.listen(setId)
            .map { set ->
                when (state) {
                    is RecordSetState.Planning -> state.copy(backingSet = set)
                    is RecordSetState.Recorded -> state.copy(backingSet = set)
                    is RecordSetState.Recording -> state.copy(backingSet = set)
                }
            }
    },
    reducers = listOf(
        Reducer { state, event ->
            when {
                (event is RecordSetEvent.PlanningEvent) && (state is RecordSetState.Planning) -> when (event) {
                    RecordSetEvent.PlanningEvent.StartRecording -> RecordSetState.Recording(
                        cameraEnabled = state.cameraEnabled,
                        metronomeState = if (state.metronomeEnabled) MetronomeState() else null,
                        cameraController = state.cameraController,
                        backingSet = state.backingSet,
                    )

                    RecordSetEvent.PlanningEvent.ToggleCamera -> state.copy(
                        cameraEnabled = !state.cameraEnabled,
                        cameraController = if (state.cameraEnabled) null else state.cameraController
                    )

                    RecordSetEvent.PlanningEvent.ToggleMetronome -> state.copy(
                        metronomeEnabled = !state.metronomeEnabled
                    )

                    RecordSetEvent.PlanningEvent.CameraPermissionGranted -> state.copy(
                        cameraController = cameraControllerFactory.create()
                    )
                }

                (event is RecordSetEvent.RecordingEvent && state is RecordSetState.Recording) -> when (event) {
                    RecordSetEvent.RecordingEvent.Stop -> when (state.cameraController) {
                        null -> RecordSetState.Planning(
                            cameraEnabled = false,
                            cameraController = state.cameraController,
                            metronomeEnabled = state.metronomeState != null,
                        )

                        else -> RecordSetState.Recorded(
                            cameraController = state.cameraController,
                            backingSet = state.backingSet,
                        )
                    }

                    RecordSetEvent.RecordingEvent.Tick -> state
                    RecordSetEvent.RecordingEvent.Beep -> state.copy(
                        metronomeState = state.metronomeState?.copy(lastBeepTime = Clock.System.now())
                    )
                }

                (event is RecordSetEvent.RecordedEvent && state is RecordSetState.Recorded) -> when (event) {
                    RecordSetEvent.RecordedEvent.ReRecordVideo -> RecordSetState.Planning(
                        cameraEnabled = state.cameraController != null,
                        cameraController = state.cameraController,
                        backingSet = state.backingSet,
                    )

                    is RecordSetEvent.RecordedEvent.SaveToSet -> state
                    is RecordSetEvent.RecordedEvent.VideoStored -> state.copy(
                        videoUri = event.videoUri
                    )
                }

                else -> state
            }
        }
    ),
    sideEffects = listOf(
        SideEffect { disp, state, event ->
            if (event is RecordSetEvent.PlanningEvent.StartRecording) {
                state.cameraController?.startRecording(FileKit.cacheDir / "${Clock.System.now()}_$setId.mp4")
                disp(RecordSetEvent.RecordingEvent.Tick)
            }
            if (event is RecordSetEvent.RecordingEvent.Stop) {
                state.cameraController?.stopRecording()
                withContext(Dispatchers.Default) {
                    delay(500)
                    val recordingPath = state.cameraController?.recordingComplete?.value
                    if (recordingPath != null) {
                        val setId = state.backingSet?.id ?: uuid4().toString()
                        dependencies.videoStorage.saveVideo(PlatformFile(recordingPath), setId)
                            .onSuccess { uri ->
                                disp(RecordSetEvent.RecordedEvent.VideoStored(uri))
                            }
                            .onFailure { throwable ->
                                throwable.printStackTrace()
                            }
                    }
                }
            }
            when (state) {
                is RecordSetState.Planning -> when (event) {
                    is RecordSetEvent.PlanningEvent -> {
                        when (event) {
                            RecordSetEvent.PlanningEvent.StartRecording -> {}
                            RecordSetEvent.PlanningEvent.ToggleCamera -> {}
                            RecordSetEvent.PlanningEvent.ToggleMetronome -> {}
                            RecordSetEvent.PlanningEvent.CameraPermissionGranted -> {}
                        }
                    }

                    else -> state
                }

                is RecordSetState.Recording -> when (event) {
                    is RecordSetEvent.RecordingEvent -> {
                        when (event) {
                            RecordSetEvent.RecordingEvent.Stop -> {}
                            RecordSetEvent.RecordingEvent.Tick -> {
                                withContext(Dispatchers.Default) {
                                    delay(50)
                                    if (
                                        (
                                            state.metronomeState
                                                ?.lastBeepTime?.until(Clock.System.now(), DateTimeUnit.SECOND) ?: 0
                                            ) >= 1L
                                    ) {
                                        disp(RecordSetEvent.RecordingEvent.Beep)
                                    }
                                    disp(RecordSetEvent.RecordingEvent.Tick)
                                }
                            }

                            RecordSetEvent.RecordingEvent.Beep -> {
                                dependencies.audioPlayer.speak("Beep")
                            }
                        }
                    }

                    else -> state
                }

                is RecordSetState.Recorded -> when (event) {
                    is RecordSetEvent.RecordedEvent -> {
                        when (event) {
                            RecordSetEvent.RecordedEvent.ReRecordVideo -> {
                                state.videoUri?.let {
                                    dependencies.videoStorage.deleteVideo(it)
                                }
                            }
                            RecordSetEvent.RecordedEvent.SaveToSet -> {
                                state.backingSet?.let {
                                    dependencies.setRepository.save(
                                        it.copy(videoUri = state.videoUri)
                                    )
                                }
                                navController.onBackPressed(false)
                            }

                            is RecordSetEvent.RecordedEvent.VideoStored -> {}
                        }
                    }

                    else -> state
                }
            }
        }
    )
)
