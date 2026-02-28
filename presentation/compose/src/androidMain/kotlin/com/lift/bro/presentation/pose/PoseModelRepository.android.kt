package com.lift.bro.presentation.pose

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

private const val MODEL_URL =
    "https://storage.googleapis.com/mediapipe-models/pose_landmarker/pose_landmarker_lite/" +
        "float16/1/pose_landmarker_lite.task"
private const val MODEL_FILE_NAME = "pose_landmarker_lite.task"
private const val PREF_KEY_DOWNLOADED = "pose_model_downloaded"

@Suppress("TooGenericExceptionCaught", "PrintStackTrace", "MagicNumber")
actual class PoseModelRepository(private val context: Context) {
    private val modelsDir: File
        get() = File(context.filesDir, "models").also {
            if (!it.exists()) it.mkdirs()
        }

    private val modelFile: File
        get() = File(modelsDir, MODEL_FILE_NAME)

    private val prefs by lazy {
        context.getSharedPreferences("pose_model_prefs", Context.MODE_PRIVATE)
    }

    actual fun isModelDownloaded(): Boolean {
        return prefs.getBoolean(PREF_KEY_DOWNLOADED, false) && modelFile.exists()
    }

    actual fun getModelPath(): String? {
        return if (isModelDownloaded()) {
            modelFile.absolutePath
        } else {
            null
        }
    }

    actual suspend fun downloadModelIfNeeded(): Result<String> = withContext(Dispatchers.IO) {
        if (isModelDownloaded()) {
            return@withContext Result.success(modelFile.absolutePath)
        }

        try {
            val url = URL(MODEL_URL)
            val connection = url.openConnection()
            connection.connect()

            connection.getInputStream().use { input ->
                FileOutputStream(modelFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int

                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                    }
                }
            }

            prefs.edit().putBoolean(PREF_KEY_DOWNLOADED, true).apply()

            Result.success(modelFile.absolutePath)
        } catch (e: Exception) {
            modelFile.delete()
            prefs.edit().putBoolean(PREF_KEY_DOWNLOADED, false).apply()
            Result.failure(e)
        }
    }

    actual fun clearModel() {
        modelFile.delete()
        prefs.edit().putBoolean(PREF_KEY_DOWNLOADED, false).apply()
    }
}

actual class PoseModelRepositoryFactory {
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    actual fun create(): PoseModelRepository {
        return PoseModelRepository(context!!)
    }
}

@Composable
actual fun rememberPoseModelRepository(): PoseModelRepositoryFactory {
    val context = androidx.compose.ui.platform.LocalContext.current
    return remember {
        PoseModelRepositoryFactory().also { it.setContext(context) }
    }
}
