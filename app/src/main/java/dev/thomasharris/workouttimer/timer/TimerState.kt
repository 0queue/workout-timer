package dev.thomasharris.workouttimer.timer

sealed class TimerState {
    abstract val phases: Phases
}

data class EditState(
    override val phases: Phases,
) : TimerState()

data class InProgressState(
    override val phases: Phases,
    val steps: List<Step>,
    val progress: Float,
    val lastTime: Long? = null,
) : TimerState() {
    val current = steps.firstOrNull()
    val isDone = steps.isEmpty()
    val isPaused = lastTime == null

    fun pop(): InProgressState = copy(steps = steps.drop(1))
}

sealed class TimerEvent {
    data class SecondsRemaining(val seconds: Int) : TimerEvent()

    data class MoveToPhase(val phase: Phase) : TimerEvent()

    // naturally reached the end
    object Done : TimerEvent()

    // started from the top
    object Start : TimerEvent()

    // pause in progress
    object Pause : TimerEvent()

    // resume in progress
    object Resume : TimerEvent()

    // stopped in progress
    object Stop : TimerEvent()

    object LastSet : TimerEvent()
}

sealed class TimerAction {
    data class Increment(val phase: Phase) : TimerAction()

    data class Decrement(val phase: Phase) : TimerAction()

    object PlayPause : TimerAction()

    data class Frame(val nanos: Long) : TimerAction()

    object Stop : TimerAction()
}

data class Step(
    val phase: Phase,
    val maxNanos: Long,
)

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
