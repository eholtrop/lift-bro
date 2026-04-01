package com.lift.bro.ui

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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.di.dependencies
import com.lift.bro.ui.dialog.InfoDialog
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.release_notes_dialog_title
import lift_bro.core.generated.resources.release_notes_row_ignore_content_description
import lift_bro.core.generated.resources.release_notes_row_new_update_title
import lift_bro.core.generated.resources.release_notes_row_tap_subtitle
import org.jetbrains.compose.resources.stringResource

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
                    text = stringResource(Res.string.release_notes_row_new_update_title),
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = stringResource(Res.string.release_notes_row_tap_subtitle),
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
                    contentDescription = stringResource(Res.string.release_notes_row_ignore_content_description)
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
        title = { Text(stringResource(Res.string.release_notes_dialog_title)) },
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
