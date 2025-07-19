package com.lift.bro.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import com.lift.bro.di.dependencies
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.theme.spacing
import com.lift.bro.utils.today
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toInstant
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import lift_bro.core.generated.resources.Res

@Composable
fun ReleaseNotesRow(
    modifier: Modifier = Modifier,
) {
    var releaseNotes: List<ReleaseNote> by remember { mutableStateOf(emptyList()) }

    val latestReleaseNote by remember { derivedStateOf { releaseNotes.maxByOrNull { it.versionId } } }
    val latestReadReleaseNotes by dependencies.settingsRepository.getLatestReadReleaseNotes().collectAsState(null)


    LaunchedEffect(Unit) {
        launch {
            releaseNotes = Json.decodeFromString(
                Res.readBytes("files/release_notes.json").decodeToString()
            )
        }
    }

    if (latestReleaseNote == null || latestReleaseNote?.versionId == latestReadReleaseNotes) return

    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        ReleaseNotesDialog {
            showDialog = false
            dependencies.settingsRepository.setLatestReadReleaseNotes(versionId = latestReleaseNote!!.versionId)
        }
    }

    Card(
        modifier = modifier,
        onClick = {
            showDialog = true
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(start = MaterialTheme.spacing.one),
            ) {
                Text(
                    text = "New Update Downloaded!",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Tap to see Release Notes",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            IconButton(
                onClick = {
                    dependencies.settingsRepository.setLatestReadReleaseNotes(versionId = latestReleaseNote!!.versionId)
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Ignore Release Notes"
                )
            }
        }
    }
}

@Composable
fun ReleaseNotesDialog(
    onDismissRequest: () -> Unit,
) {
    InfoDialog(
        title = { Text("Release Notes") },
        message = {
            var releaseNotes: List<ReleaseNote> by remember { mutableStateOf(emptyList()) }

            LaunchedEffect(Unit) {
                launch {
                    releaseNotes = Json.decodeFromString(
                        Res.readBytes("files/release_notes.json").decodeToString()
                    )
                }
            }

            releaseNotes.groupBy { it.versionId }.forEach {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        it.key,
                        style = MaterialTheme.typography.titleLarge
                    )
                    it.value.forEach {
                        Row {
                            Text("•")
                            Space(MaterialTheme.spacing.one)
                            Text(it.note.en)
                        }
                    }
                    Space(MaterialTheme.spacing.half)
                }
            }
        },
        onDismissRequest = onDismissRequest
    )
}

@Serializable
data class ReleaseNote(
    val versionId: String,
    val type: String,
    val platform: String,
    val note: Note,
)

@Serializable
data class Note(
    val en: String,
)