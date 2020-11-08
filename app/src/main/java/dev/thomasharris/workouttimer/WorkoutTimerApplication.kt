package dev.thomasharris.workouttimer

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.DefaultAnimationClock
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue

class WorkoutTimerApplication : Application() {
    val mainViewModel = MainViewModel()
    private val prefsManager by lazy { PrefsManager(this) }
    val settingsViewModel by lazy { SettingsViewModel(prefsManager) }

    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        clock = DefaultAnimationClock(),
    )

    override fun onCreate() {
        super.onCreate()

        settingsViewModel.stateFlow.value.nightMode.toMode()
            .let(AppCompatDelegate::setDefaultNightMode)
    }
}

val AppCompatActivity.mainViewModel: MainViewModel
    get() = (applicationContext as WorkoutTimerApplication).mainViewModel

val AppCompatActivity.settingsViewModel: SettingsViewModel
    get() = (applicationContext as WorkoutTimerApplication).settingsViewModel

@OptIn(ExperimentalMaterialApi::class)
val AppCompatActivity.bottomSheetState: ModalBottomSheetState
    get() = (applicationContext as WorkoutTimerApplication).bottomSheetState