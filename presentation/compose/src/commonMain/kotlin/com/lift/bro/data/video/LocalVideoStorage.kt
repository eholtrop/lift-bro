package com.lift.bro.data.video

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.copyTo
import io.github.vinceglb.filekit.delete
import io.github.vinceglb.filekit.div
import io.github.vinceglb.filekit.filesDir
import io.github.vinceglb.filekit.name

class LocalVideoStorage : VideoStorage {

    private val videosDir: PlatformFile
        get() = FileKit.filesDir / "videos"

    override suspend fun saveVideo(sourceFile: PlatformFile, setId: String): Result<String> {
        return try {
            val videoFile = videosDir / "${setId}.mp4"
            sourceFile.copyTo(videoFile)
            Result.success("local://${videoFile.name}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVideo(videoUri: String): Result<Unit> {
        return try {
            val videoFile = videosDir / videoUri.removePrefix("local://")
            videoFile.delete()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getVideoFile(videoUri: String): PlatformFile {
        return videosDir / videoUri.removePrefix("local://")
    }

}
