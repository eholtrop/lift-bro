package com.lift.bro.presentation.backup

import androidx.compose.runtime.Composable
import com.lift.bro.Backup
import com.lift.bro.data.datasource.flowToList
import com.lift.bro.di.dependencies
import com.lift.bro.di.liftRepository
import com.lift.bro.di.setRepository
import com.lift.bro.di.variationRepository
import com.lift.bro.di.workoutRepository
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.domain.repositories.Setting
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.cacheDir
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.take
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import tv.dpal.ext.flow.mapEach
import tv.dpal.ext.ktx.datetime.toString
import tv.dpal.flowvi.Interactor
import tv.dpal.flowvi.Reducer
import tv.dpal.flowvi.SideEffect
import tv.dpal.flowvi.rememberInteractor
import kotlin.time.Clock

typealias BackupInteractor = Interactor<BackupState, BackupEvent>

@Serializable
data class BackupState(
    val backup: Backup,
    val backupFinished: Boolean = false,
    @Transient val file: PlatformFile? = null,
)

sealed interface BackupEvent {
    data object BackupFinished: BackupEvent
    data class ShareFile(val file: PlatformFile): BackupEvent
}

fun <T> Flow<T>.nullable(): Flow<T?> = this

@Composable
fun rememberBackupInteractor(): BackupInteractor = rememberInteractor(
    initialState = BackupState(Backup()),
    source = {
        combine(
            dependencies.liftRepository.listenAll().nullable().take(1).onStart { emit(null) },
            dependencies.variationRepository.listenAll().nullable().take(1).onStart { emit(null) },
            dependencies.setRepository.listenAll().nullable().take(1).onStart { emit(null) },
            dependencies.workoutRepository.getAll().nullable().take(1).onStart { emit(null) },
            dependencies.database.logDataSource.getAll().flowToList().take(1).mapEach {
                LiftingLog(
                    id = it.id,
                    date = it.date,
                    notes = it.notes ?: "",
                    vibe = it.vibe_check?.toInt(),
                )
            }.nullable().onStart { emit(null) },
        ) { categories, movements, sets, workouts, logs ->
            BackupState(
                Backup(
                    lifts = categories,
                    variations = movements,
                    sets = sets,
                    workouts = workouts,
                    exercises = workouts?.flatMap { it.exercises },
                    liftingLogs = logs
                ),
                backupFinished = categories != null &&
                    movements != null &&
                    sets != null &&
                    workouts != null &&
                    logs != null,
            )
        }
    },
    reducers = listOf(
        Reducer { state, event ->
            when (event) {
                is BackupEvent.ShareFile -> state.copy(file = event.file)
                else -> state
            }
        }
    ),
    sideEffects = listOf(
        SideEffect { disp, state, event ->
            when (event) {
                is BackupEvent.BackupFinished -> {
                    val backupDir = FileKit.cacheDir / "backups"
                    if (!backupDir.exists()) {
                        backupDir.createDirectories()
                    }
                    val backupFile = backupDir / "${Clock.System.now().toString("yyyy-MM-dd_HH:mm:ss")}.json"
                    backupFile.writeString(Json.encodeToString(state.backup))

                    disp(BackupEvent.ShareFile(backupFile))
                }

                is BackupEvent.ShareFile -> {
                    dependencies.settingsRepository.set(
                        Setting.BackupSettings,
                        BackupSettings(lastBackupDate = Clock.System.todayIn(TimeZone.currentSystemDefault()))
                    )
                }
            }
        }
    )
)
