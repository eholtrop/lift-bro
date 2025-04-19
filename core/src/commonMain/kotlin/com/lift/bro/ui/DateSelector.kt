package com.lift.bro.ui


import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.lift.bro.presentation.toString
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DateSelector(
    modifier: Modifier = Modifier,
    date: Instant,
    dateChanged: (Instant) -> Unit
) {
    var openDialog by remember { mutableStateOf(false) }

    val pickerState = rememberDatePickerState(
        date.toLocalDateTime(TimeZone.currentSystemDefault()).toInstant(
            TimeZone.UTC
        ).toEpochMilliseconds()
    )

    if (openDialog) {
        DatePickerDialog(
            onDismissRequest = { openDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        openDialog = false
                        dateChanged(
                            Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!)
                                .toLocalDateTime(TimeZone.UTC)
                                .toInstant(TimeZone.currentSystemDefault())
                        )
                    },
                    enabled = pickerState.selectedDateMillis != null,
                ) {
                    Text("Ok")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { openDialog = false }
                ) {
                    Text("Close")
                }
            }
        ) {
            DatePicker(
                state = pickerState
            )
        }
    }

    LineItem(
        modifier = modifier,
        title = "On",
        description = Instant.fromEpochMilliseconds(pickerState.selectedDateMillis!!)
            .toLocalDateTime(TimeZone.UTC)
            .toInstant(TimeZone.currentSystemDefault())
            .toString("MMMM d - yyyy"),
        onClick = {
            openDialog = true
        }
    )
}