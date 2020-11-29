package dev.thomasharris.workouttimer.ui

import androidx.compose.animation.DpPropKey
import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AmbientEmphasisLevels
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProvideEmphasis
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.timer.EditState
import dev.thomasharris.workouttimer.timer.InProgressState
import dev.thomasharris.workouttimer.timer.Phase
import dev.thomasharris.workouttimer.timer.TimerState
import dev.thomasharris.workouttimer.timer.TimerViewModel
import kotlin.math.ceil
import kotlin.math.roundToInt
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.thomasharris.workouttimer.SMALL_ANIMATION_TIME_MS

private val elevationPropKey = DpPropKey()

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

private val opacityPropKey = FloatPropKey()

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
        phase == Phase.SETS -> AmbientEmphasisLevels.current.high
        state is InProgressState && state.current?.phase != phase -> AmbientEmphasisLevels.current.disabled
        else -> AmbientEmphasisLevels.current.high
    }

    val buttonEmphasis = when (state) {
        is InProgressState -> AmbientEmphasisLevels.current.disabled
        else -> AmbientEmphasisLevels.current.high
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
                ProvideEmphasis(emphasis = textEmphasis) {
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
                    ProvideEmphasis(emphasis = buttonEmphasis) {
                        IconButton(
                            enabled = state is EditState,
                            modifier = Modifier.padding(start = 32.dp),
                            onClick = { onClick(PhaseCardEvent.DECREMENT) },
                        ) {
                            Icon(asset = Icons.Default.ArrowLeft.scale(2f))
                        }
                    }
                    ProvideEmphasis(emphasis = textEmphasis) {
                        Text(
                            state.valueOfFormatted(phase = phase),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body1.copy(fontSize = 32.sp),
                            modifier = Modifier.padding(8.dp).weight(1f),
                        )
                    }

                    ProvideEmphasis(emphasis = buttonEmphasis) {
                        IconButton(
                            enabled = state is EditState,
                            modifier = Modifier.padding(end = 32.dp),
                            onClick = { onClick(PhaseCardEvent.INCREMENT) },
                        ) {
                            Icon(asset = Icons.Default.ArrowRight.scale(2f))
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
                        .drawOpacity(opacityTransitionState[opacityPropKey]),
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

fun VectorAsset.scale(scale: Float): VectorAsset =
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