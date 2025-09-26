package com.lift.bro.data.file

import com.lift.bro.domain.backup.BackupDescriptor
import com.lift.bro.domain.backup.BackupSnapshot
import com.lift.bro.domain.backup.BackupTarget
import com.lift.bro.domain.backup.FileDataSource
import com.lift.bro.domain.backup.RestoreSource
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okio.FileSystem
import okio.Path
import okio.Path.Companion.toPath
import okio.buffer
import okio.use

class JsonFileDataSource(
    private val rootDir: Path,
    private val fileSystem: FileSystem = FileSystem.SYSTEM,
    private val json: Json = Json { prettyPrint = false; ignoreUnknownKeys = true }
) : FileDataSource {

    override suspend fun write(target: BackupTarget, snapshot: BackupSnapshot): BackupDescriptor {
        val path = resolve(target.logicalPath)
        val parent = path.parent
        if (parent != null && !fileSystem.exists(parent)) {
            fileSystem.createDirectories(parent)
        }
        val bytes = json.encodeToString(snapshot).encodeToByteArray()
        fileSystem.sink(path).buffer().use { sink ->
            sink.write(bytes)
            sink.flush()
        }
        return BackupDescriptor(
            id = kotlin.random.Random.nextLong().toString(),
            createdAtEpochMs = kotlin.system.getTimeMillis(),
            sizeBytes = bytes.size.toLong(),
            target = target
        )
    }

    override suspend fun read(source: RestoreSource): BackupSnapshot {
        val path = resolve(source.logicalPath)
        fileSystem.source(path).buffer().use { sourceBuf ->
            val content = sourceBuf.readUtf8()
            return json.decodeFromString(content)
        }
    }

    private fun resolve(logicalPath: String): Path =
        if (logicalPath.startsWith("/")) logicalPath.toPath() else rootDir / logicalPath
}
