package dev.thomasharris.workouttimer.timer

import androidx.compose.animation.core.FloatPropKey
import androidx.compose.animation.core.transitionDefinition
import androidx.compose.animation.core.tween
import androidx.compose.animation.transition
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawOpacity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.R
import dev.thomasharris.workouttimer.SMALL_ANIMATION_TIME_MS
import dev.thomasharris.workouttimer.ui.PhaseCard
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import dev.thomasharris.workouttimer.ui.PlayButton
import dev.thomasharris.workouttimer.ui.scale
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme

private val scalePropKey = FloatPropKey()

private val scaleTransitionDefinition = transitionDefinition<Boolean> {
    state(true) {
        this[scalePropKey] = 1f
    }

    state(false) {
        this[scalePropKey] = 0f
    }

    transition(true to false, false to true) {
        scalePropKey using tween(durationMillis = SMALL_ANIMATION_TIME_MS)
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerScreen(
    state: TimerState,
    ttsReady: Boolean,
    onPlayButtonClicked: () -> Unit,
    onPhaseClicked: (Phase, PhaseCardEvent) -> Unit,
    onStopClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {

    val canPlay = state is EditState || (state is InProgressState && state.isPaused)

    val scaleTransitionState = transition(
        definition = scaleTransitionDefinition,
        toState = state is InProgressState,
    )

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            title = {
                Text(
                    text = stringResource(id = R.string.app_name),
                    // TODO alpha07 has a bug? looks normal in preview, is not styled when run
                    style = MaterialTheme.typography.h6
                )
            },
            elevation = 0.dp,
            actions = {
                if ((1f - scaleTransitionState[scalePropKey]) > .1f) IconButton(
                    onClick = onSettingsClicked,
                    modifier = Modifier.drawOpacity(1f - scaleTransitionState[scalePropKey])
                ) {
                    Icon(asset = Icons.Default.Settings)
                }
            }
        )
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollableColumn {
                Phase.values().forEach { phase ->
                    PhaseCard(
                        state = state,
                        phase = phase,
                        onClick = {
                            onPhaseClicked(phase, it)
                        },
                    )
                }

                Spacer(modifier = Modifier.preferredSize((8 + 56).dp))
            }

            PlayButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                onClick = onPlayButtonClicked,
                enabled = if (state is EditState) ttsReady else true
            ) {
                val icon = if (canPlay) Icons.Default.PlayArrow else Icons.Default.Pause
                Icon(
                    modifier = Modifier.padding(4.dp),
                    asset = icon.scale(2f),
                )
            }

            // TODO less math
            // TODO find out the proper way to animate visibility changes
            //   I dislike making it about a threshold during a transition
            //   but it also kind of makes a lot of sense
            if (scaleTransitionState[scalePropKey] > .1f)
                PlayButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp + ((24 + 4) * (1f - scaleTransitionState[scalePropKey])).dp),
                    onClick = onStopClicked,
                    color = MaterialTheme.colors.secondary,
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(4.dp),
                        asset = Icons.Default.Stop.scale(2f * scaleTransitionState[scalePropKey])
                    )
                }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TimerScreenPreview() {
    WorkoutTimerTheme {
        TimerScreen(
            state = TimerViewModel.DEFAULT_STATE,
            onPlayButtonClicked = {},
            onPhaseClicked = { _, _ -> },
            onStopClicked = {},
            onSettingsClicked = {},
            ttsReady = true
        )
    }
}