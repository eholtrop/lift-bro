package com.lift.bro.data

import com.lift.bro.di.dependencies
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.LBSet
import com.lift.bro.domain.models.Variation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

//expect class FileManager {
//    fun saveToFile(path: String, fileName: String, content: String)
//}

object BackupRestore {

    suspend fun backup(): Backup {
        return Backup(
            lifts = dependencies.database.liftDataSource.getAll().first(),
            variations = dependencies.database.variantDataSource.getAll(),
            sets = dependencies.database.setDataSource.getAll()
        )
    }

    fun restore(json: String) {
        flow {
            emit(Json.decodeFromString(json))
        }.flatMapLatest {
            restore(it as Backup)
        }
    }

    fun restore(backup: Backup): Flow<Unit> = flow {
        dependencies.database.liftDataSource.clear()
        dependencies.database.variantDataSource.deleteAll()
        dependencies.database.setDataSource.deleteAll()

        backup.lifts.forEach {
            dependencies.database.liftDataSource.save(it)
        }
        backup.variations.forEach {
            dependencies.database.variantDataSource.save(
                id = it.id,
                liftId = it.liftId,
                name = it.name
            )
        }
        backup.sets.forEach {
            dependencies.database.setDataSource.save(it)
        }
        emit(Unit)
    }
}

@Serializable
data class Backup(
    val lifts: List<Lift>,
    val variations: List<Variation>,
    val sets: List<LBSet>,
)