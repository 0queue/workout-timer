package dev.thomasharris.workouttimer.ui

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.ButtonElevation
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.ui.tooling.preview.Preview

/**
 * An IconButton with a surface behind it essentially, and raise on click behaviour
 */
@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PlayButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    color: Color = MaterialTheme.colors.primary,
    interactionState: InteractionState = remember { InteractionState() },
    elevation: ButtonElevation = ButtonDefaults.elevation(),
    enabled: Boolean = true,
    content: @Composable () -> Unit,
) {
    Surface(
        elevation = elevation.elevation(true, interactionState),
        color = color,
        shape = CircleShape,
        modifier = modifier
    ) {
        Box(
            Modifier.clickable(
                onClick = onClick,
                interactionState = interactionState,
                enabled = enabled
            ),
            contentAlignment = Alignment.Center,
            content = {
                content()
            },
        )
    }
}

@Preview
@Composable
fun PlayButtonPreview() {
    PlayButton {
        Icon(imageVector = Icons.Default.PlayArrow.scale(2f))
    }
}