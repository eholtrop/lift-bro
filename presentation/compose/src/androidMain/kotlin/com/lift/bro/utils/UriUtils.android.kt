package com.lift.bro.utils

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import androidx.core.net.toUri
import tv.dpal.logging.Log
import tv.dpal.logging.d

actual fun convertContentUriToFileUri(contentUri: String, context: Any?): String {
    val androidContext = context as Context
    val uri = resolveCameraLibraryUri(androidContext, contentUri)
    val fileName = contentUri.split("/").last()
    val file = File(androidContext.cacheDir, fileName)

    androidContext.contentResolver.openInputStream(uri)?.use { inputStream ->
        FileOutputStream(file).use { outputStream ->
            inputStream.copyTo(outputStream)
        }
    } ?: run {
    }

    Log.d(message = file.absolutePath)

    return "file://${file.absolutePath}"
}

fun resolveCameraLibraryUri(context: Context, badUriString: String): Uri {
    if (!badUriString.endsWith(".mp4")) {
        return Uri.parse(badUriString)
    }

    val fileName = badUriString.substringAfterLast("/")

    val projection = arrayOf(MediaStore.Video.Media._ID)
    val selection = "${MediaStore.Video.Media.DISPLAY_NAME} = ?"
    val selectionArgs = arrayOf(fileName)

    context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        selection,
        selectionArgs,
        null
    )?.use { cursor ->
        if (cursor.moveToFirst()) {
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val realId = cursor.getLong(idColumn)

            // 4. Construct the proper URI (e.g., content://media/external/video/media/123)
            return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, realId)
        }
    }

    Log.d(message = "failed query")
    // Fallback just in case the query fails
    return Uri.parse(badUriString)
}
