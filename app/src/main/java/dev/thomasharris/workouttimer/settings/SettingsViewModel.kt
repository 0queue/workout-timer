package dev.thomasharris.workouttimer.settings

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.mapNotNull

private const val NIGHT_MODE_KEY = "NIGHT_MODE"

class SettingsViewModel(
    private val prefsManager: PrefsManager,
) {

    val stateFlow = prefsManager.stateFlow.mapNotNull { (k, v) ->
        if (k == NIGHT_MODE_KEY) {
            val mode = State.NightMode.fromString(v)
            State(mode)
        } else null
    }

    fun setNightMode(nightMode: State.NightMode) {
        prefsManager.putString(NIGHT_MODE_KEY, nightMode.toString())
    }

    suspend fun register() {
        prefsManager.register(NIGHT_MODE_KEY)
    }

    data class State(
        val nightMode: NightMode?,
    ) {
        enum class NightMode {
            Day,
            Night,
            System;

            companion object {

                val DEFAULT = System

                fun fromString(s: String?): NightMode {
                    if (s == null)
                        return DEFAULT

                    return try {
                        valueOf(s)
                    } catch (ex: IllegalArgumentException) {
                        DEFAULT
                    }
                }
            }

            fun toMode(): Int = when (this) {
                Day -> AppCompatDelegate.MODE_NIGHT_NO
                Night -> AppCompatDelegate.MODE_NIGHT_YES
                System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        }

        companion object {
            val UNINITIALIZED = State(null)
        }
    }
}

