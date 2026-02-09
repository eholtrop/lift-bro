package tv.dpal.compose

import androidx.compose.ui.platform.SoftwareKeyboardController

@androidx.compose.runtime.Composable
actual fun SoftwareKeyboardController?.isOpen(): Boolean = false
