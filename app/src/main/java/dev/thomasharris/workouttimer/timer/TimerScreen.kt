package dev.thomasharris.workouttimer.timer

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.R
import dev.thomasharris.workouttimer.ui.PhaseCard
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import dev.thomasharris.workouttimer.ui.PlayButton
import dev.thomasharris.workouttimer.ui.scale
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TimerScreen(
    state: TimerViewState,
    ttsReady: Boolean,
    onPlayButtonClicked: () -> Unit,
    onPhaseClicked: (Phase, PhaseCardEvent) -> Unit,
    onStopClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
) {

    val canPlay = state is EditState || (state is InProgressState && state.isPaused)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            title = { Text(stringResource(id = R.string.app_name)) },
            elevation = 0.dp,
            actions = {
                if (state is EditState) IconButton(onClick = onSettingsClicked) {
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

            if (state is InProgressState)
                PlayButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    onClick = onStopClicked,
                    color = MaterialTheme.colors.secondary,
                ) {
                    Icon(
                        modifier = Modifier.padding(4.dp),
                        asset = Icons.Default.Stop.scale(2f)
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