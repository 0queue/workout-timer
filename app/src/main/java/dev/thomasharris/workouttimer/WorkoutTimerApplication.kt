package dev.thomasharris.workouttimer

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.animation.core.DefaultAnimationClock
import androidx.compose.material.BottomDrawerState
import androidx.compose.material.BottomDrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material.ModalBottomSheetValue

class WorkoutTimerApplication : Application() {
    val mainViewModel = MainViewModel()

    @OptIn(ExperimentalMaterialApi::class)
    val bottomSheetState = ModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        clock = DefaultAnimationClock(),
    )

    override fun onCreate() {
        super.onCreate()

        // TODO
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
    }
}

val AppCompatActivity.mainViewModel: MainViewModel
    get() = (applicationContext as WorkoutTimerApplication).mainViewModel

@OptIn(ExperimentalMaterialApi::class)
val AppCompatActivity.bottomSheetState: ModalBottomSheetState
    get() = (applicationContext as WorkoutTimerApplication).bottomSheetState