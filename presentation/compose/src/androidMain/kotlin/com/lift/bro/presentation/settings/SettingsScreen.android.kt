package com.lift.bro.presentation.settings

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.toClipEntry

actual fun String.toClipEntry(): ClipEntry {
    return ClipData.newPlainText(this, this).toClipEntry()
}
