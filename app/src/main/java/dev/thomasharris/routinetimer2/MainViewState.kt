package dev.thomasharris.routinetimer2

sealed class MainViewState

data class EditState(
    val phases: Phases,
) : MainViewState()

data class InProgressState(
    val phases: Phases,
    val currentPhase: String, // TODO enum
    val progress: Float,
    val isPaused: Boolean,
) : MainViewState()

data class Phases(
    val prepTimeSeconds: Int,
    val workTimeSeconds: Int,
    val restTimeSeconds: Int,
    val sets: Int,
)