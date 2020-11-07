package dev.thomasharris.routinetimer2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.AnimationClockAmbient
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.routinetimer2.ui.PhaseCard
import dev.thomasharris.routinetimer2.ui.PhaseCardEvent
import dev.thomasharris.routinetimer2.ui.PlayButton
import dev.thomasharris.routinetimer2.ui.scale
import dev.thomasharris.routinetimer2.ui.theme.RoutineTimer2Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val state by mainViewModel.stateFlow.collectAsState()

            AnimationClockAmbient

            RoutineTimer2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    MainScreen(
                        state = state,
                        onPlayButtonClicked = mainViewModel::onToggle,
                        onPhaseClicked = mainViewModel::onPhaseClicked
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    state: MainViewState,
    onPlayButtonClicked: () -> Unit,
    onPhaseClicked: (Phase, PhaseCardEvent) -> Unit,
) {

    val canPlay = state is EditState || (state is InProgressState && state.isPaused)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            backgroundColor = MaterialTheme.colors.surface,
            title = { Text(stringResource(id = R.string.app_name)) },
            elevation = 0.dp,
        )
        Box(modifier = Modifier.fillMaxSize()) {
            ScrollableColumn {
                Phase.values().forEach { phase ->
                    PhaseCard(state = state, phase = phase, onClick = {
                        onPhaseClicked(phase, it)
                    })
                }

                Spacer(modifier = Modifier.preferredSize((8 + 56).dp))
            }

            PlayButton(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp),
                onClick = onPlayButtonClicked,
            ) {
                val icon = if (canPlay) Icons.Default.PlayArrow else Icons.Default.Pause
                Icon(
                    modifier = Modifier.padding(4.dp),
                    asset = icon.scale(2f),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    RoutineTimer2Theme {
        MainScreen(
            state = MainViewModel().stateFlow.value,
            onPlayButtonClicked = {},
            onPhaseClicked = { _, _ -> },
        )
    }
}