package dev.thomasharris.workouttimer

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.DefaultAnimationClock
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue
import dev.thomasharris.workouttimer.settings.PrefsManager
import dev.thomasharris.workouttimer.settings.SettingsViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WorkoutTimerApplication : Application() {
    val mainViewModel by lazy { MainViewModel(AndroidWakeLocker(this)) }
    private val prefsManager by lazy { PrefsManager(this) }
    val settingsViewModel by lazy { SettingsViewModel(prefsManager) }

    private val scope = CoroutineScope(Dispatchers.Main)

    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        clock = DefaultAnimationClock(),
    )

    override fun onCreate() {
        super.onCreate()

        scope.launch {
            settingsViewModel.register()
            settingsViewModel.stateFlow.collect {
                delay(150) // short delay to let button animation finish
                it.nightMode
                    ?.toMode()
                    ?.let(AppCompatDelegate::setDefaultNightMode)
            }
        }
    }
}

val AppCompatActivity.mainViewModel: MainViewModel
    get() = (applicationContext as WorkoutTimerApplication).mainViewModel

val AppCompatActivity.settingsViewModel: SettingsViewModel
    get() = (applicationContext as WorkoutTimerApplication).settingsViewModel

@OptIn(ExperimentalMaterialApi::class)
val AppCompatActivity.bottomSheetState: ModalBottomSheetState
    get() = (applicationContext as WorkoutTimerApplication).bottomSheetState