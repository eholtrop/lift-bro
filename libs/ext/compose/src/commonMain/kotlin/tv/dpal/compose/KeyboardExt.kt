package tv.dpal.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.SoftwareKeyboardController

@Composable
expect fun SoftwareKeyboardController?.isOpen(): Boolean
