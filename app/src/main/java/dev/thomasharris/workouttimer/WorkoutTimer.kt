package dev.thomasharris.workouttimer

import android.app.Application
import androidx.appcompat.app.AppCompatActivity

class WorkoutTimer : Application() {
    val mainViewModel = MainViewModel()
}

val AppCompatActivity.mainViewModel: MainViewModel
    get() = (applicationContext as WorkoutTimer).mainViewModel