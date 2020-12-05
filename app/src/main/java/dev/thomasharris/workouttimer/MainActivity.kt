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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import dev.chrisbanes.accompanist.insets.ProvideWindowInsets
import dev.chrisbanes.accompanist.insets.navigationBarsPadding
import dev.chrisbanes.accompanist.insets.statusBarsPadding
import dev.chrisbanes.accompanist.insets.systemBarsPadding
import dev.thomasharris.workouttimer.settings.SettingsSheet
import dev.thomasharris.workouttimer.settings.SettingsViewState
import dev.thomasharris.workouttimer.timer.TimerEvent
import dev.thomasharris.workouttimer.timer.TimerScreen
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
                    is TimerEvent.SecondsRemaining -> ttsManager.speak(event.seconds.toString())
                    is TimerEvent.MoveToPhase -> ttsManager.speak(event.phase.displayName)
                    TimerEvent.Done -> ttsManager.speak("Done")
                    TimerEvent.Start -> ttsManager.speak("Ready")
                    TimerEvent.LastSet -> ttsManager.speak("Last Set")
                    else -> Unit
                }
            }
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)

        setContent {
            val timerViewState by timerViewModel.stateFlow.collectAsState()
            val settingsState by settingsViewModel.stateFlow.collectAsState(SettingsViewState.UNINITIALIZED)
            val ttsState by ttsManager.isReady.collectAsState()

            WorkoutTimerTheme {
                ProvideWindowInsets {
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
                                    onNightModeSelected = settingsViewModel::setNightMode,
                                    modifier = Modifier.navigationBarsPadding()
                                )
                            }
                        ) {
                            TimerScreen(
                                state = timerViewState,
                                onPlayButtonClicked = timerViewModel::onToggle,
                                onPhaseClicked = timerViewModel::onPhaseClicked,
                                onStopClicked = timerViewModel::onStopClicked,
                                onSettingsClicked = { settingsSheetState.show() },
                                ttsReady = ttsState,
                                modifier = Modifier.systemBarsPadding()
                            )
                        }
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
