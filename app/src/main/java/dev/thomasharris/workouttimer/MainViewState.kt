package dev.thomasharris.workouttimer

import kotlin.math.floor

sealed class MainViewState {
    abstract val phases: Phases
}

data class EditState(
    override val phases: Phases,
) : MainViewState()

data class Step(
    val phase: Phase,
    val maxNanos: Long,
)

data class InProgressState(
    override val phases: Phases,
    val steps: List<Step>,
    val progress: Float,
    val lastTime: Long? = null,
) : MainViewState() {
    val current = steps.firstOrNull()
    val isDone = steps.isEmpty()
    val isPaused = lastTime == null

    fun pop(): InProgressState = when {
        isDone -> this
        else -> copy(steps = steps.drop(1))
    }
}

sealed class Event {
    data class SecondsRemaining(val seconds: Int) : Event()

    data class MoveToPhase(val phase: Phase) : Event()

    // naturally reached the end
    object Done : Event()

    // started from the top
    object Start : Event()

    // pause in progress
    object Pause : Event()

    // resume in progress
    object Resume : Event()

    // stopped in progress
    object Stop : Event()

    object LastSet : Event()
}

fun EditState.toInProgressState(): InProgressState {

    val steps = mutableListOf(
        Step(Phase.PREP, phases.prepTimeSeconds.nanos)
    )

    for (i in 0 until phases.sets - 1) {
        steps.add(Step(Phase.WORK, phases.workTimeSeconds.nanos))
        steps.add(Step(Phase.REST, phases.restTimeSeconds.nanos))
    }

    steps.add(Step(Phase.WORK, phases.workTimeSeconds.nanos))

    return InProgressState(
        phases = phases,
        steps = steps,
        progress = 0f,
        lastTime = System.nanoTime()
    )
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

    object Stop : Action()
}

fun MainViewState.accept(action: Action): Pair<MainViewState, Event?> {
    return when (val state = this) {
        is EditState -> state.accept(action)
        is InProgressState -> when (action) {
            is Action.Frame -> {
                when {
                    state.lastTime == null -> state to null
                    state.isDone -> EditState(state.phases) to Event.Done
                    state.progress >= 1f -> {
                        state.pop().copy(progress = 0f).let { ips ->
                            ips to ips.steps.firstOrNull()?.phase?.let { phase ->
                                val setsRemaining = ips.steps
                                    .filter { it.phase == Phase.WORK }
                                    .count()

                                if (phase == Phase.WORK && setsRemaining == 1)
                                    Event.LastSet
                                else
                                    Event.MoveToPhase(phase)
                            }
                        }
                    }
                    state.current != null -> {
                        // TODO move out somewhere
                        val deltaT = action.nanos - state.lastTime
                        val percentProgress =
                            deltaT.toFloat() / state.current.maxNanos.toFloat()

                        val newProgress = state.progress + percentProgress

                        val lastSecond =
                            ((state.current.maxNanos / 1_000_000_000L) * (1f - state.progress))
                                .let(::floor)
                                .toInt()
                        val curSecond =
                            ((state.current.maxNanos / 1_000_000_000L) * (1f - newProgress))
                                .let(::floor)
                                .toInt()

                        val event = if (lastSecond != curSecond && curSecond in (0..2)) {
                            Event.SecondsRemaining(curSecond + 1)
                        } else null


                        state.copy(
                            progress = newProgress,
                            lastTime = action.nanos
                        ) to event
                    }
                    else -> state to null
                }
            }
            is Action.PlayPause -> state.copy(lastTime = if (state.isPaused) System.nanoTime() else null)
                .let { it to if (it.isPaused) Event.Pause else Event.Resume }
            is Action.Stop -> EditState(state.phases) to Event.Stop
            else -> state to null
        }
    }
}

fun EditState.accept(action: Action): Pair<MainViewState, Event?> {
    return when (action) {
        is Action.Increment -> {
            val increment = if (action.phase == Phase.SETS) 1 else 5

            val newPhases = when (action.phase) {
                Phase.PREP -> phases.copy(prepTimeSeconds = phases.prepTimeSeconds + increment)
                Phase.WORK -> phases.copy(workTimeSeconds = phases.workTimeSeconds + increment)
                Phase.REST -> phases.copy(restTimeSeconds = phases.restTimeSeconds + increment)
                Phase.SETS -> phases.copy(sets = phases.sets + increment)
            }

            copy(phases = newPhases) to null
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

            copy(phases = newPhases) to null
        }
        is Action.PlayPause -> toInProgressState() to Event.Start
        else -> this to null
    }
}

val Int.nanos: Long
    get() = this * 1_000_000_000L
