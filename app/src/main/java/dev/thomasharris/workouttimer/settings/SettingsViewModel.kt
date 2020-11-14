package dev.thomasharris.workouttimer.settings

import kotlinx.coroutines.flow.mapNotNull

private const val NIGHT_MODE_KEY = "NIGHT_MODE"

class SettingsViewModel(
    private val prefsManager: PrefsManager,
) {

    val stateFlow = prefsManager.stateFlow.mapNotNull { (k, v) ->
        if (k == NIGHT_MODE_KEY) {
            val mode = SettingsViewState.NightMode.fromString(v)
            SettingsViewState(mode)
        } else null
    }

    fun setNightMode(nightMode: SettingsViewState.NightMode) {
        prefsManager.putString(NIGHT_MODE_KEY, nightMode.toString())
    }

    suspend fun register() {
        prefsManager.register(NIGHT_MODE_KEY)
    }

}

