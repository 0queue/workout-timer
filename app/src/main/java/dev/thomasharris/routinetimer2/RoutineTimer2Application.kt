package dev.thomasharris.routinetimer2

import android.app.Application
import androidx.appcompat.app.AppCompatActivity

class RoutineTimer2Application : Application() {
    val mainViewModel = MainViewModel()
}

val AppCompatActivity.mainViewModel: MainViewModel
    get() = (applicationContext as RoutineTimer2Application).mainViewModel