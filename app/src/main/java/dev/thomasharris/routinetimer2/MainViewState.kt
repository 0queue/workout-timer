package dev.thomasharris.routinetimer2

import android.util.Log

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
    val lastTime: Long?,
) : MainViewState() {
    val isPaused = lastTime == null
}

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

sealed class Action {
    data class Increment(val phase: Phase) : Action()

    data class Decrement(val phase: Phase) : Action()

    object PlayPause : Action()

    data class Frame(val nanos: Long) : Action()
}

fun MainViewState.accept(action: Action): MainViewState {
    return when (val state = this) {
        is EditState -> state.accept(action)
        is InProgressState -> when (action) {
            is Action.Frame -> {
                if (state.lastTime == null) {
                    state
                } else if (state.progress >= 1f) {
                    EditState(state.phases)
                } else {
                    val deltaT = action.nanos - state.lastTime
                    val totalT = when (state.currentPhase) {
                        Phase.PREP -> state.phases.prepTimeSeconds
                        Phase.WORK -> state.phases.workTimeSeconds
                        Phase.REST -> state.phases.restTimeSeconds
                        Phase.SETS -> throw IllegalStateException("not actually a phase")
                    } * 1_000_000_000L

                    val percentProgress = deltaT.toFloat() / totalT.toFloat()

                    state.copy(
                        progress = state.progress + percentProgress,
                        lastTime = action.nanos
                    )
                }
            }
            is Action.PlayPause -> {
                state.copy(lastTime = if (state.lastTime == null) System.nanoTime() else null)
            }
            else -> state
        }
    }
}

fun EditState.accept(action: Action): MainViewState {
    return when (action) {
        is Action.Increment -> {
            val increment = if (action.phase == Phase.SETS) 1 else 5

            val newPhases = when (action.phase) {
                Phase.PREP -> phases.copy(prepTimeSeconds = phases.prepTimeSeconds + increment)
                Phase.WORK -> phases.copy(workTimeSeconds = phases.workTimeSeconds + increment)
                Phase.REST -> phases.copy(restTimeSeconds = phases.restTimeSeconds + increment)
                Phase.SETS -> phases.copy(sets = phases.sets + increment)
            }

            copy(phases = newPhases)
        }
        is Action.Decrement -> {
            val decrement = if (action.phase == Phase.SETS) 1 else 5

            // ew
            val newPhases = when (action.phase) {
                Phase.PREP -> if (phases.prepTimeSeconds - decrement >= 5)
                    phases.copy(prepTimeSeconds = phases.prepTimeSeconds - decrement)
                else phases
                Phase.WORK -> if (phases.workTimeSeconds - decrement >= 5)
                    phases.copy(workTimeSeconds = phases.workTimeSeconds - decrement)
                else phases
                Phase.REST -> if (phases.restTimeSeconds - decrement >= 5)
                    phases.copy(restTimeSeconds = phases.restTimeSeconds - decrement)
                else phases
                Phase.SETS -> if (phases.sets - decrement >= 1)
                    phases.copy(sets = phases.sets - decrement)
                else phases
            }

            copy(phases = newPhases)
        }
        is Action.PlayPause -> {
            InProgressState(phases, Phase.PREP, 0f, System.nanoTime())
        }
        else -> this // TODO
    }
}