package com.lift.bro.ui.theme

import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons as MaterialIcons

object Icons {
    val notes = MaterialIcons.AutoMirrored.Default.Notes
    val favourite = MaterialIcons.Default.Favorite
}

val MaterialTheme.icons get() = Icons
