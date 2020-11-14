package dev.thomasharris.workouttimer.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.withContext

class PrefsManager(
    context: Context,
) {
    private val prefs = context.getSharedPreferences("PREFERENCES", Context.MODE_PRIVATE)

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { p, k ->
        val v = p.getString(k, null)
        _channel.value = k to v
    }

    private val _channel = MutableStateFlow<Pair<String, String?>?>(null)
    val stateFlow: Flow<Pair<String, String?>> = _channel.asStateFlow().filterNotNull()

    suspend fun register(initialKey: String) = withContext(Dispatchers.IO) {
        prefs.registerOnSharedPreferenceChangeListener(listener)
        listener.onSharedPreferenceChanged(prefs, initialKey)
    }

    fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}