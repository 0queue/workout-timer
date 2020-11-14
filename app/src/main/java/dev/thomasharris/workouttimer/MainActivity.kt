package dev.thomasharris.workouttimer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import dev.thomasharris.workouttimer.timer.Event
import dev.thomasharris.workouttimer.timer.TimerScreen
import dev.thomasharris.workouttimer.settings.SettingsSheet
import dev.thomasharris.workouttimer.settings.SettingsViewState
import dev.thomasharris.workouttimer.ui.displayName
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme
import dev.thomasharris.workouttimer.util.TextToSpeechManager
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
            timerViewModel.eventFlow.collect { event ->
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
            val timerViewState by timerViewModel.stateFlow.collectAsState()
            val settingsState by settingsViewModel.stateFlow.collectAsState(SettingsViewState.UNINITIALIZED)
            val ttsState by ttsManager.isReady.collectAsState()

            WorkoutTimerTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    ModalBottomSheetLayout(
                        sheetState = settingsSheetState,
                        sheetShape = MaterialTheme.shapes.large.copy(
                            topLeft = CornerSize(16.dp),
                            topRight = CornerSize(16.dp)
                        ),
                        sheetContent = {
                            SettingsSheet(
                                settingsViewState = settingsState,
                                onNightModeSelected = settingsViewModel::setNightMode
                            )
                        }
                    ) {
                        TimerScreen(
                            state = timerViewState,
                            onPlayButtonClicked = timerViewModel::onToggle,
                            onPhaseClicked = timerViewModel::onPhaseClicked,
                            onStopClicked = timerViewModel::onStopClicked,
                            onSettingsClicked = { settingsSheetState.show() },
                            ttsReady = ttsState
                        )
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalMaterialApi::class)
    override fun onBackPressed() {
        if (settingsSheetState.isVisible)
            settingsSheetState.hide()
        else
            super.onBackPressed()
    }
}
