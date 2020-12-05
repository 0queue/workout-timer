package dev.thomasharris.workouttimer.ui

import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AmbientContentAlpha
import androidx.compose.material.Card
import androidx.compose.material.ContentAlpha
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Providers
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.SMALL_ANIMATION_TIME_MS
import dev.thomasharris.workouttimer.timer.EditState
import dev.thomasharris.workouttimer.timer.InProgressState
import dev.thomasharris.workouttimer.timer.Phase
import dev.thomasharris.workouttimer.timer.TimerState
import dev.thomasharris.workouttimer.timer.TimerViewModel
import kotlin.math.ceil
import kotlin.math.roundToInt

private val elevationPropKey = DpPropKey(label = "elevation")

private val elevationTransitionDefinition = transitionDefinition<Boolean> {
    state(true) {
        this[elevationPropKey] = 4.dp
    }

    state(false) {
        this[elevationPropKey] = 0.dp
    }

    transition(true to false, false to true) {
        elevationPropKey using tween(durationMillis = SMALL_ANIMATION_TIME_MS)
    }
}

private val opacityPropKey = FloatPropKey(label = "opacity")

private val opacityTransitionDefinition = transitionDefinition<Boolean> {
    state(true) {
        this[opacityPropKey] = 1f
    }

    state(false) {
        this[opacityPropKey] = 0f
    }

    transition(true to false, false to true) {
        opacityPropKey using tween(durationMillis = SMALL_ANIMATION_TIME_MS)
    }
}

@Composable
fun PhaseCard(
    state: TimerState,
    phase: Phase,
    onClick: (PhaseCardEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val textEmphasis = when {
        phase == Phase.SETS -> ContentAlpha.high
        state is InProgressState && state.current?.phase != phase -> ContentAlpha.disabled
        else -> ContentAlpha.high
    }

    val buttonEmphasis = when (state) {
        is InProgressState -> ContentAlpha.disabled
        else -> ContentAlpha.high
    }

    val elevationTransitionState = transition(
        definition = elevationTransitionDefinition,
        toState = state !is InProgressState || (state.current?.phase == phase),
    )

    val opacityTransitionState = transition(
        definition = opacityTransitionDefinition,
        toState = state is InProgressState && state.current?.phase == phase
    )

    var progress by remember {
        mutableStateOf(0f)
    }

    if (state is InProgressState && state.current?.phase == phase)
        progress = 1f - state.progress.coerceIn(0f, 1f)

    Card(
        modifier = modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = elevationTransitionState[elevationPropKey],
    ) {
        Box {
            Column(
                modifier = Modifier.padding(8.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Providers(AmbientContentAlpha provides textEmphasis) {
                    Text(
                        phase.displayName,
                        style = MaterialTheme.typography.h4,
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Providers(AmbientContentAlpha provides buttonEmphasis) {
                        IconButton(
                            enabled = state is EditState,
                            modifier = Modifier.padding(start = 32.dp),
                            onClick = { onClick(PhaseCardEvent.DECREMENT) },
                        ) {
                            Icon(imageVector = Icons.Default.ArrowLeft.scale(2f))
                        }
                    }
                    Providers(AmbientContentAlpha provides textEmphasis) {
                        Text(
                            state.valueOfFormatted(phase = phase),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body1.copy(fontSize = 32.sp),
                            modifier = Modifier.padding(8.dp).weight(1f),
                        )
                    }

                    Providers(AmbientContentAlpha provides buttonEmphasis) {
                        IconButton(
                            enabled = state is EditState,
                            modifier = Modifier.padding(end = 32.dp),
                            onClick = { onClick(PhaseCardEvent.INCREMENT) },
                        ) {
                            Icon(imageVector = Icons.Default.ArrowRight.scale(2f))
                        }
                    }
                }
            }

            // TODO custom progress indicator that is taller? .height doesn't seem to work
            if (opacityTransitionState[opacityPropKey] > .1f)
                LinearProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .alpha(opacityTransitionState[opacityPropKey]),
                    progress = progress // 1f - state.progress.coerceIn(0f, 1f)
                )
        }
    }
}

@Composable
@Preview
fun PhaseCardPreview() {
    val state = TimerViewModel.DEFAULT_STATE
    PhaseCard(state = state, phase = Phase.PREP, onClick = {})
}

enum class PhaseCardEvent {
    INCREMENT,
    DECREMENT,
}

fun ImageVector.scale(scale: Float): ImageVector =
    copy(defaultWidth = defaultWidth * scale, defaultHeight = defaultHeight * scale)

val Phase.displayName: String
    get() = when (this) {
        Phase.PREP -> "Preparation" // TODO str resources
        Phase.WORK -> "Work"
        Phase.REST -> "Rest"
        Phase.SETS -> "Sets"
    }

fun TimerState.valueOfFormatted(phase: Phase): String {
    var n = when (phase) {
        Phase.PREP -> phases.prepTimeSeconds
        Phase.WORK -> phases.workTimeSeconds
        Phase.REST -> phases.restTimeSeconds
        Phase.SETS -> phases.sets
    }

    if (phase == Phase.SETS) {// TODO current set?
        if (this is InProgressState)
            n = steps.filter { it.phase == Phase.WORK }.count()

        return n.toString()
    }
    // ew
    if (this is InProgressState && current?.phase == phase) {
        n = n
            .times(1f - progress)
            .let(::ceil)
            .roundToInt()
            .coerceAtLeast(1)// TODO picked up for smoothness, necessary?
    }

    return n.asTime()
}

fun Int.asTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}