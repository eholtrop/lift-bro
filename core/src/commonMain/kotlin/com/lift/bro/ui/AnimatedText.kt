package com.lift.bro.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.lift.bro.presentation.excercise.ExcerciseDetailsScreen

object AnimatedTextDefaults {
    val transitionForChar: AnimatedContentTransitionScope<Char>.(Char, Int) -> ContentTransform = { char, index ->
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
    color: Color = Color.Unspecified,
    transitionForChar: AnimatedContentTransitionScope<Char>.(Char, Int) -> ContentTransform = AnimatedTextDefaults.transitionForChar,
) {
    Row(
        modifier = modifier,
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
                    color = color
                )
            }
        }
    }
}