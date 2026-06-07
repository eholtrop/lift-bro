package com.lift.bro.ui

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.lift.bro.ui.theme.spacing
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import lift_bro.core.generated.resources.Res
import lift_bro.core.generated.resources.release_notes_release_notes_close_content_description
import lift_bro.core.generated.resources.release_notes_row_new_update_title
import lift_bro.core.generated.resources.release_notes_row_tap_subtitle
import org.jetbrains.compose.resources.stringResource

@Composable
fun ReleaseNotesRow(
    modifier: Modifier = Modifier,
    dialogSeen: () -> Unit,
    rowDismissed: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        ReleaseNotesDialog {
            showDialog = false
            dialogSeen()
        }
    }

    com.lift.bro.ui.banner.DashboardBanner(
        modifier = modifier,
        onClick = {
            showDialog = true
        },
        onDismiss = rowDismissed,
        onClickLabel = stringResource(Res.string.release_notes_release_notes_close_content_description),
    ) {
        Column(
            modifier = Modifier.padding(start = MaterialTheme.spacing.one),
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
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ReleaseNotesDialog(
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
    ) {
        var releaseNotes: List<ReleaseNote> by remember { mutableStateOf(emptyList()) }

        LaunchedEffect(Unit) {
            launch {
                releaseNotes = Json.decodeFromString(
                    Res.readBytes("files/release_notes.json").decodeToString()
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.one),
            horizontalAlignment = Alignment.Start
        ) {
            releaseNotes.groupBy { it.versionId }.forEach {
                Column(
                    modifier = Modifier.fillMaxWidth().scrollable(
                        rememberScrollableState { it },
                        orientation = Orientation.Vertical
                    ),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        it.key,
                        style = MaterialTheme.typography.titleLarge
                    )
                    it.value.forEach { version ->
                        version.notes.forEach { note ->
                            Row {
                                Text("•")
                                Space(MaterialTheme.spacing.one)
                                Text(note.en)
                            }
                        }
                    }
                }
            }
            Space(MaterialTheme.spacing.one)
        }
    }
}

@Serializable
data class ReleaseNote(
    val versionId: String,
    val notes: List<Note>,
)

@Serializable
data class Note(
    val type: String,
    val platform: String,
    val en: String,
)
