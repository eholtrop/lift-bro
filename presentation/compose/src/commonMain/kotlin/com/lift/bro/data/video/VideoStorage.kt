package com.lift.bro.data.video

import java.io.File

interface VideoStorage {
    suspend fun saveVideo(sourceFile: File, setId: String): Result<String>
    suspend fun deleteVideo(videoUri: String): Result<Unit>
    fun getVideoFile(videoUri: String): File?
}
