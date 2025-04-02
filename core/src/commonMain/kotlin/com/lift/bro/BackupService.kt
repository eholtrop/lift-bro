package com.lift.bro

import com.lift.bro.data.Backup
import com.lift.bro.di.dependencies
import kotlinx.coroutines.flow.first

expect object BackupService {

    suspend fun backup(backup: Backup = createBackup())

    fun restore()

}

fun createBackup(): Backup {
    return Backup(
        lifts = dependencies.database.liftDataSource.getAll(),
        variations = dependencies.database.variantDataSource.getAll(),
        sets = dependencies.database.setDataSource.getAll()
    )
}