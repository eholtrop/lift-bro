package com.lift.bro

import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.LiftingLog
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.utils.toString
import com.lift.bro.utils.today
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.createDirectories
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.exists
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Backup(
    val lifts: List<Lift>? = null,
    val variations: List<Variation>? = null,
    val sets: List<LBSet>? = null,
    val liftingLogs: List<LiftingLog>? = null,
)

object BackupService {

    suspend fun backup(backup: Backup = createBackup()) {
        val backupDir = FileKit.filesDir / "backups"
        if (!backupDir.exists()) {
            backupDir.createDirectories()
        }
        val backupFile = backupDir / "${Clock.System.now().toString("yyyy-MM-dd_HH:mm:ss")}.json"
        backupFile.writeString(Json.encodeToString(backup))
        FileKit.shareFile(backupFile)
        dependencies.settingsRepository.saveBackupSettings(BackupSettings(lastBackupDate = Clock.System.today))
    }

    suspend fun restore(): Boolean {
        val backupDir = FileKit.filesDir / "backups"
        if (!backupDir.exists()) {
            backupDir.createDirectories()
        }
        FileKit.openFilePicker(
            type = FileKitType.File("application/json"),
            directory = backupDir
        )?.apply {
            restore(Json.decodeFromString<Backup>(readString()))
            return true
        } ?: kotlin.run {
            return false
        }
        return false
    }

    suspend fun restore(backup: Backup): Boolean {
        dependencies.database.liftDataSource.deleteAll()
        dependencies.database.variantDataSource.deleteAll()
        dependencies.database.setDataSource.deleteAll()
        dependencies.database.logDataSource.deleteAll()


        backup.sets?.forEach {
            dependencies.database.setDataSource.save(it)
        }

        backup.variations?.forEach {
            dependencies.database.variantDataSource.save(
                id = it.id,
                liftId = it.lift!!.id,
                name = it.name,
            )
        }

        backup.lifts?.forEach {
            dependencies.database.liftDataSource.save(it)
        }

        backup.liftingLogs?.forEach {
            dependencies.database.logDataSource.save(
                id = it.id,
                notes = it.notes,
                date = it.date,
                vibe_check = it.vibe?.toLong(),
            )
        }

        return true
    }

}

fun createBackup(): Backup {
    return Backup(
        lifts = dependencies.database.liftDataSource.getAll(),
        variations = dependencies.database.variantDataSource.getAll(),
        sets = dependencies.database.setDataSource.getAll(),
        liftingLogs = dependencies.database.logDataSource.getAll().executeAsList().map {
            LiftingLog(
                id = it.id,
                date = it.date,
                notes = it.notes ?: "",
                vibe = it.vibe_check?.toInt(),
            )
        },
    )
}