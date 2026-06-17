package com.lift.bro.data.video

import io.github.vinceglb.filekit.PlatformFile

interface VideoStorage {
    suspend fun saveVideo(sourceFile: PlatformFile, setId: String): Result<String>
    suspend fun deleteVideo(videoUri: String): Result<Unit>
    fun getVideoFile(videoUri: String): PlatformFile?
}
