package dev.thomasharris.workouttimer.settings

import androidx.appcompat.app.AppCompatDelegate

data class SettingsViewState(
    val nightMode: NightMode?,
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

    companion object {
        val UNINITIALIZED = SettingsViewState(null)
    }
}