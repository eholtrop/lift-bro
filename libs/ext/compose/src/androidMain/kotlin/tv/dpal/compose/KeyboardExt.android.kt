package tv.dpal.compose

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.SoftwareKeyboardController

@androidx.compose.runtime.Composable
actual fun SoftwareKeyboardController?.isOpen() = WindowInsets.ime.getBottom(LocalDensity.current) > 0
