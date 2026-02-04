package com.lift.bro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.lift.bro.utils.DarkModeProvider
import com.lift.bro.utils.PreviewAppTheme
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.jetbrains.compose.ui.tooling.preview.PreviewParameter

object AnimatedTextDefaults {
    val slideInOut: AnimatedContentTransitionScope<Char>.(Char, Int) -> ContentTransform = { char, index ->
        if (initialState < targetState) {
            slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
        } else {
            slideInVertically { -it } + fadeIn() togetherWith slideOutVertically { it } + fadeOut()
        }
    }
}

@Composable
fun AnimatedText(
    modifier: Modifier = Modifier,
    text: String,
    style: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign = TextAlign.Center,
    color: Color = Color.Unspecified,
    transitionForChar: AnimatedContentTransitionScope<Char>.(
        Char,
        Int,
    ) -> ContentTransform = AnimatedTextDefaults.slideInOut,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.toCharArray().forEachIndexed { index, char ->
            AnimatedContent(
                targetState = char,
                label = "",
                transitionSpec = { transitionForChar(char, index) }
            ) {
                Text(
                    text = it.toString(),
                    style = style,
                    textAlign = textAlign,
                    color = color
                )
            }
        }
    }
}

@Preview
@Composable
fun AnimatedTextPreview(@PreviewParameter(DarkModeProvider::class) darkMode: Boolean) {
    PreviewAppTheme(isDarkMode = darkMode) {
        var counter by remember { mutableStateOf(0) }

        // Demonstrates animated text with changing numbers
        AnimatedText(
            text = "Count: $counter",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Static example
        AnimatedText(
            text = "225 lbs",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
