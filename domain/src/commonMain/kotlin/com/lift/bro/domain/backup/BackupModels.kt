package com.lift.bro.domain.backup

import com.lift.bro.domain.models.Exercise
import com.lift.bro.domain.models.Lift
import com.lift.bro.domain.models.Variation
import com.lift.bro.domain.models.Workout
import kotlinx.serialization.Serializable

@JvmInline
value class BackupId(val value: String)

@Serializable
data class BackupTarget(
    val logicalPath: String,
)

@Serializable
data class RestoreSource(
    val logicalPath: String,
)

@Serializable
data class BackupDescriptor(
    val id: String,
    val createdAtEpochMs: Long,
    val sizeBytes: Long,
    val target: BackupTarget
)

@Serializable
data class RestoreReport(
    val inserted: Int,
    val updated: Int,
    val skipped: Int,
)

@Serializable
data class BackupSnapshot(
    val lifts: List<Lift>,
    val variations: List<Variation>,
    val workouts: List<Workout>
)

interface FileDataSource {
    suspend fun write(target: BackupTarget, snapshot: BackupSnapshot): BackupDescriptor
    suspend fun read(source: RestoreSource): BackupSnapshot
}