package dev.thomasharris.workouttimer

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val NIGHT_MODE_KEY = "NIGHT_MODE"

class SettingsViewModel(
    private val prefsManager: PrefsManager,
) {

    // TODO figure out better night mode initialization
    //   currently the blocking causes a slight hitch that is a nice
    //   glaring white screen essentially
    private val _stateFlow = MutableStateFlow(State(
        nightMode = prefsManager.getString(NIGHT_MODE_KEY)
            .let(State.NightMode.Companion::fromString)
    ))

    val stateFlow = _stateFlow.asStateFlow()

    fun setNightMode(nightMode: State.NightMode) {
        if (nightMode != _stateFlow.value.nightMode) {
            prefsManager.putString("NIGHT_MODE", nightMode.toString())
            _stateFlow.value = _stateFlow.value.copy(nightMode = nightMode)
            //TODO delay slightly?
            AppCompatDelegate.setDefaultNightMode(nightMode.toMode())
        }
    }

    data class State(
        val nightMode: NightMode,
    ) {
        enum class NightMode {
            Day,
            Night,
            System;

            companion object {

                private val DEFAULT = System

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
    }
}

class PrefsManager(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)

    fun getString(key: String): String? {
        return prefs.getString(key, null) // TODO yeah yeah it's blocking
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}