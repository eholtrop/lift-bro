package com.lift.bro

import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import com.lift.bro.data.Backup
import com.lift.bro.di.DependencyContainer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date

actual object BackupService {

    actual suspend fun backup(backup: Backup) {

        val context = DependencyContainer.context!!

        val json = Json.encodeToString(backup)

        val backupPath = File(context.filesDir, "backups")
        if (!backupPath.exists()) {
            backupPath.mkdir()
        }
        val backupFile = File(
            backupPath,
            "${
                SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS").format(
                    Date()
                )
            }.json"
        )

        with(FileWriter(backupFile)) {
            append(json)
            flush()
            close()
        }

        val backupUri = FileProvider.getUriForFile(
            context,
            "com.lift.bro.fileprovider",
            backupFile
        )

        context.startActivity(
            ShareCompat.IntentBuilder(context)
                .setStream(backupUri)
                .setType("application/json")
                .createChooserIntent()
                .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        )
    }

    actual fun restore() {
        DependencyContainer.context?.activity?.startActivityForResult(
            Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "application/json"
            },
            0
        )
    }
}

val Context?.activity: androidx.core.app.ComponentActivity? get() = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> this.baseContext.activity
    else -> null
}