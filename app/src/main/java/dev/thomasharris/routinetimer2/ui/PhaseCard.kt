package dev.thomasharris.routinetimer2.ui

import androidx.compose.foundation.AmbientContentColor
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.routinetimer2.EditState
import dev.thomasharris.routinetimer2.InProgressState
import dev.thomasharris.routinetimer2.MainViewModel
import dev.thomasharris.routinetimer2.MainViewState
import dev.thomasharris.routinetimer2.Phase
import kotlin.math.ceil
import kotlin.math.roundToInt

private val disabled: Color = Color.Gray.copy(alpha = .6f)

@Composable
fun PhaseCard(
    state: MainViewState,
    phase: Phase,
    onClick: (PhaseCardEvent) -> Unit,
) {
    val textColor = if (state is InProgressState && state.currentPhase != phase)
        disabled
    else
        Color.Unspecified

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    phase.displayName,
                    style = MaterialTheme.typography.h4,
                    color = textColor
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    IconButton(
                        enabled = state is EditState,
                        modifier = Modifier.padding(start = 32.dp),
                        onClick = { onClick(PhaseCardEvent.DECREMENT) },
                    ) {
                        Icon(
                            tint = if (state is EditState) AmbientContentColor.current else disabled,
                            asset = Icons.Default.ArrowLeft.scale(2f),
                        )
                    }
                    Text(
                        state.valueOfFormatted(phase = phase),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.body1.copy(fontSize = 32.sp),
                        modifier = Modifier.padding(8.dp).weight(1f),
                        color = textColor
                    )
                    IconButton(
                        enabled = state is EditState,
                        modifier = Modifier.padding(end = 32.dp),
                        onClick = { onClick(PhaseCardEvent.INCREMENT) },
                    ) {
                        Icon(
                            tint = if (state is EditState) AmbientContentColor.current else disabled,
                            asset = Icons.Default.ArrowRight.scale(2f),
                        )
                    }
                }
            }

            // TODO custom progress indicator that is taller? .height doesn't seem to work
            if (state is InProgressState && state.currentPhase == phase)
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth(),
                    progress = 1f - state.progress.coerceIn(0f, 1f)
                )
        }
    }
}

@Composable
@Preview
fun PhaseCardPreview() {
    val state = MainViewModel().stateFlow.value
    PhaseCard(state = state, phase = Phase.PREP, onClick = {})
}

enum class PhaseCardEvent {
    INCREMENT,
    DECREMENT,
}

fun VectorAsset.scale(scale: Float): VectorAsset =
    copy(defaultWidth = defaultWidth * scale, defaultHeight = defaultHeight * scale)

val Phase.displayName: String
    get() = when (this) {
        Phase.PREP -> "Preparation"
        Phase.WORK -> "Work"
        Phase.REST -> "Rest"
        Phase.SETS -> "Sets"
    }

fun MainViewState.valueOfFormatted(phase: Phase): String {
    var n = when (phase) {
        Phase.PREP -> phases.prepTimeSeconds
        Phase.WORK -> phases.workTimeSeconds
        Phase.REST -> phases.restTimeSeconds
        Phase.SETS -> phases.sets
    }

    if (phase == Phase.SETS) // TODO current set?
        return n.toString()

    // ew
    if (this is InProgressState && currentPhase == phase) {
       n = n
           .times(1f - progress)
           .let(::ceil)
           .roundToInt()
    }

    return n.asTime()
}

fun Int.asTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}