package dev.thomasharris.workouttimer.settings

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.gesture.pressIndicatorGestureFilter
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.AmbientUriHandler
import androidx.compose.ui.selection.AmbientTextSelectionColors
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.ui.tooling.preview.Preview

// adapted from https://stackoverflow.com/questions/64739312/autolink-for-android-compose-text
@Composable
fun GithubLinkText(
    modifier: Modifier = Modifier,
) {
    val uriHandler = AmbientUriHandler.current
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var pressedUrl by remember { mutableStateOf<String?>(null) }

    // newline is a compromise with the existing text breaking strategies, which
    // is basically just "wrap or not"
    val text = "Check for the latest release at\n"
    val link = "github.com/0queue/workout-timer/releases"
    val annotatedString = buildAnnotatedString {
        append(text)
        // not entirely satisfied with primary and text selection colors,
        // but have no idea where to find proper link colors
        pushStyle(style = SpanStyle(
            color = MaterialTheme.colors.primary,
            textDecoration = TextDecoration.Underline,
            background = if (pressedUrl != null) {
                AmbientTextSelectionColors.current.backgroundColor
            } else
                Color.Unspecified
        ))
        append(link)
        addStringAnnotation(
            tag = "URL",
            annotation = "https://github.com/0queue/workout-timer/releases",
            start = text.length,
            end = text.length + link.length
        )
    }

    Text(
        text = annotatedString,
        textAlign = TextAlign.Center,
        modifier = modifier.pressIndicatorGestureFilter(onStart = { offset ->
            layoutResult?.let { textLayoutResult ->
                val position = textLayoutResult.getOffsetForPosition(offset)
                annotatedString.getStringAnnotations(position, position).firstOrNull()
                    ?.let { stringAnnotation ->
                        if (stringAnnotation.tag == "URL") {
                            pressedUrl = stringAnnotation.item
                        }
                    }
            }
        }, onStop = {
            pressedUrl?.let(uriHandler::openUri)
            pressedUrl = null
        }, onCancel = { pressedUrl = null }),
        onTextLayout = { layoutResult = it },
    )
}

@Composable
@Preview
fun GithubLinkTextPreview() {
    GithubLinkText()
}