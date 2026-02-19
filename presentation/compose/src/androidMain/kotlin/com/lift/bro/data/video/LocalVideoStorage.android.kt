package com.lift.bro.data.video

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

actual class LocalVideoStorage(
    private val context: Context,
) : VideoStorage {

    private val videosDir: File
        get() = File(context.filesDir, VIDEOS_DIR).also { it.mkdirs() }

    override suspend fun saveVideo(sourceFile: File, setId: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val videoFile = File(videosDir, "${setId}.mp4")
            sourceFile.copyTo(videoFile, overwrite = true)
            Result.success("local://${videoFile.name}")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteVideo(videoUri: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val fileName = videoUri.removePrefix("local://")
            val videoFile = File(videosDir, fileName)
            if (videoFile.exists()) {
                videoFile.delete()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getVideoFile(videoUri: String): File? {
        val fileName = videoUri.removePrefix("local://")
        return File(videosDir, fileName).takeIf { it.exists() }
    }

    companion object {
        private const val VIDEOS_DIR = "videos"
    }
}
