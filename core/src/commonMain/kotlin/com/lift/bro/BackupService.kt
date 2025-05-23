package com.lift.bro

import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.repositories.BackupSettings
import com.lift.bro.utils.toString
import com.lift.bro.utils.today
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.shareFile
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.readString
import io.github.vinceglb.filekit.writeString
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class Backup(
    val lifts: List<Lift>,
    val variations: List<Variation>,
    val sets: List<LBSet>,
)

object BackupService {

    suspend fun backup(backup: Backup = createBackup()) {
        val backupFile = FileKit.filesDir / "backups/${Clock.System.now().toString("yyyy-MM-dd_HH:mm:ss")}.json"
        backupFile.writeString(Json.encodeToString(backup))
        FileKit.shareFile(backupFile)
        dependencies.settingsRepository.saveBackupSettings(BackupSettings(lastBackupDate = Clock.System.today))
    }

    suspend fun restore() {
        FileKit.openFilePicker(
            type = FileKitType.File("application/json"),
            directory = FileKit.filesDir / "backups"
        )?.apply {
            restore(Json.decodeFromString<Backup>(readString()))
        }
    }

    suspend fun restore(backup: Backup) {
        dependencies.database.liftDataSource.deleteAll()
        dependencies.database.variantDataSource.deleteAll()
        dependencies.database.setDataSource.deleteAll()

        backup.lifts.forEach {
            dependencies.database.liftDataSource.save(it)
        }

        backup.variations.forEach {
            dependencies.database.variantDataSource.save(
                id = it.id,
                liftId = it.lift!!.id,
                name = it.name,
            )
        }

        backup.sets.forEach {
            dependencies.database.setDataSource.save(it)
        }
    }

}

fun createBackup(): Backup {
    return Backup(
        lifts = dependencies.database.liftDataSource.getAll(),
        variations = dependencies.database.variantDataSource.getAll(),
        sets = dependencies.database.setDataSource.getAll()
    )
}