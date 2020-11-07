package dev.thomasharris.routinetimer2

sealed class MainViewState {
    abstract val phases: Phases
}

data class EditState(
    override val phases: Phases,
) : MainViewState()

// to generate in progress state:
// PREP
// FOR EACH SET-1
//   WORK
//   REST
// WORK
// Each Step is a Phase plus a Progress plus a MaxNanos
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

fun EditState.toInProgressState2(): InProgressState {

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

fun MainViewState.accept(action: Action): MainViewState {
    return when (val state = this) {
        is EditState -> state.accept(action)
        is InProgressState -> when (action) {
            is Action.Frame -> {
                when {
                    state.lastTime == null -> state
                    state.isDone -> EditState(state.phases)
                    state.progress >= 1f -> {
                        state.pop().copy(progress = 0f)
                    }
                    state.current != null -> {
                        val deltaT = action.nanos - state.lastTime
                        val percentProgress =
                            deltaT.toFloat() / state.current.maxNanos.toFloat()

                        state.copy(
                            progress = state.progress + percentProgress,
                            lastTime = action.nanos
                        )
                    }
                    else -> state
                }
            }
            is Action.PlayPause -> {
                state.copy(lastTime = if (state.isPaused) System.nanoTime() else null)
            }
            is Action.Stop -> {
                EditState(state.phases)
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
        is Action.PlayPause -> toInProgressState2()
        else -> this // TODO
    }
}

val Int.nanos: Long
    get() = this * 1_000_000_000L