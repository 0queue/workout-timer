package dev.thomasharris.workouttimer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.ScrollableColumn
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.preferredSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.RadioButton
import androidx.compose.material.Surface
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.ui.PhaseCard
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import dev.thomasharris.workouttimer.ui.PlayButton
import dev.thomasharris.workouttimer.ui.displayName
import dev.thomasharris.workouttimer.ui.scale
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val ttsManager = TextToSpeechManager(this)

    @OptIn(ExperimentalCoroutinesApi::class, ExperimentalMaterialApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycle.addObserver(ttsManager)

        lifecycleScope.launch {
            mainViewModel.eventFlow.collect { event ->
                when (event) {
                    is Event.SecondsRemaining -> ttsManager.speak(event.seconds.toString())
                    is Event.MoveToPhase -> ttsManager.speak(event.phase.displayName)
                    Event.Done -> ttsManager.speak("Done")
                    Event.Start -> ttsManager.speak("Ready")
                    Event.LastSet -> ttsManager.speak("Last Set")
                    else -> Unit
                }
            }
        }

        setContent {
            val mainViewState by mainViewModel.stateFlow.collectAsState()
            val settingsState by settingsViewModel.stateFlow.collectAsState()

            WorkoutTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ModalBottomSheetLayout(
                        sheetState = bottomSheetState,
                        sheetShape = MaterialTheme.shapes.large.copy(
                            topLeft = CornerSize(16.dp),
                            topRight = CornerSize(16.dp)
                        ),
                        sheetContent = {
                            BottomSheet(
                                state = settingsState,
                                onNightModeSelected = settingsViewModel::setNightMode
                            )
                        }
                    ) {
                        MainScreen(
                            state = mainViewState,
                            onPlayButtonClicked = mainViewModel::onToggle,
                            onPhaseClicked = mainViewModel::onPhaseClicked,
                            onStopClicked = mainViewModel::onStopClicked,
                            onSettingsClicked = { bottomSheetState.show() }
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBackPressed() {
        if (bottomSheetState.isVisible)
            bottomSheetState.hide()
        else
            super.onBackPressed()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainScreen(
    state: MainViewState,
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

@Composable
fun BottomSheet(
    state: SettingsViewModel.State,
    onNightModeSelected: (SettingsViewModel.State.NightMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            style = MaterialTheme.typography.h5,
            text = "Settings",
        )

        val debugText = if (BuildConfig.BUILD_TYPE == "debug") " (debug)" else ""
        val caption =
            "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_CODE}${debugText}"
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.caption.copy(fontStyle = FontStyle.Italic),
            text = caption,
        )

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            SettingsViewModel.State.NightMode.values().forEach {
                NightModeRadioButton(
                    state = state,
                    nightMode = it,
                    onNightModeSelected = onNightModeSelected
                )
            }
        }
    }
}

@Composable
fun NightModeRadioButton(
    state: SettingsViewModel.State,
    nightMode: SettingsViewModel.State.NightMode,
    onNightModeSelected: (SettingsViewModel.State.NightMode) -> Unit,
) {
    val interactionState = remember { InteractionState() }
    Row {
        RadioButton(
            interactionState = interactionState,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            selected = state.nightMode == nightMode,
            onClick = { onNightModeSelected(nightMode) }
        )
        Text(
            modifier = Modifier
                .clickable(
                    interactionState = interactionState,
                    indication = null,
                    onClick = { onNightModeSelected(nightMode) },
                )
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            text = nightMode.toString(),
        )
    }
}

@Preview(showBackground = true)
@Composable
fun Preview() {
    WorkoutTimerTheme {
        MainScreen(
            state = MainViewModel().stateFlow.value,
            onPlayButtonClicked = {},
            onPhaseClicked = { _, _ -> },
            onStopClicked = {},
            onSettingsClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBottomSheet() {
    WorkoutTimerTheme {
        BottomSheet(
            state = SettingsViewModel.State(nightMode = SettingsViewModel.State.NightMode.System),
            onNightModeSelected = {},
        )
    }
}