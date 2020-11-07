package dev.thomasharris.routinetimer2

sealed class MainViewState {
    abstract val phases: Phases
}

data class EditState(
    override val phases: Phases,
) : MainViewState()

data class InProgressState(
    override val phases: Phases,
    val currentPhase: Phase,
    val progress: Float,
    val isPaused: Boolean,
) : MainViewState()

data class Phases(
    val prepTimeSeconds: Int,
    val workTimeSeconds: Int,
    val restTimeSeconds: Int,
    val sets: Int,
)

enum class Phase {
    PREP,
    WORK,
    REST,
    SETS,
}