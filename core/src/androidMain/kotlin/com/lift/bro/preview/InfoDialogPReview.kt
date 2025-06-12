package com.lift.bro.preview

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.lift.bro.ui.dialog.InfoDialog
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun InfoDialogPreview(isDark: Boolean) {
    PreviewAppTheme(true) {
        InfoDialog(
            title = { Text("Congrats!!") },
            message = {
                Text("A grouping of Variations (or Movements)\n" +
                        "\n" +
                        "ex: A Squat can have two variations, Front and Back Squat\n" +
                        "Once you name your lift you can start creating Variations of that lift!\n" +
                        "Think of it as the \"suffix\" of a movement\n" +
                        "\n" +
                        "Romanian *Deadlift*\n" +
                        "Front *Squat*\n" +
                        "Inverted *Bench Press*\n"
                )
            },
            onDismissRequest = {},
        )
    }
}

@Preview
@Composable
fun InfoDialogPreview_Light() {
    InfoDialogPreview(false)
}

@Preview
@Composable
fun InfoDialogPreview_Dark() {
    InfoDialogPreview(true)
}