package dev.thomasharris.workouttimer.timer

import kotlin.math.floor


fun reducer(state: TimerState, action: TimerAction): Pair<TimerState, TimerEvent?> = when (state) {
    is EditState -> state reduce action
    is InProgressState -> state reduce action
}

infix fun EditState.reduce(action: TimerAction): Pair<TimerState, TimerEvent?> = when (action) {
    is TimerAction.Increment -> {
        val increment = if (action.phase == Phase.SETS) 1 else 5

        val newPhases = when (action.phase) {
            Phase.PREP -> phases.copy(prepTimeSeconds = phases.prepTimeSeconds + increment)
            Phase.WORK -> phases.copy(workTimeSeconds = phases.workTimeSeconds + increment)
            Phase.REST -> phases.copy(restTimeSeconds = phases.restTimeSeconds + increment)
            Phase.SETS -> phases.copy(sets = phases.sets + increment)
        }

        copy(phases = newPhases) to null
    }
    is TimerAction.Decrement -> {
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
    is TimerAction.PlayPause -> toInProgressState() to TimerEvent.Start
    else -> this to null
}


infix fun InProgressState.reduce(action: TimerAction) = when (action) {
    is TimerAction.Frame -> {
        when {
            lastTime == null -> this to null
            isDone -> EditState(phases) to TimerEvent.Done // this branch is probably redundant
            progress >= 1f -> {
                pop().copy(progress = 0f).let { ips ->
                    if (ips.isDone)
                        EditState(phases) to TimerEvent.Done
                    else ips to ips.steps.firstOrNull()?.phase?.let { phase ->
                        val setsRemaining = ips.steps
                            .filter { it.phase == Phase.WORK }
                            .count()

                        if (phase == Phase.WORK && setsRemaining == 1)
                            TimerEvent.LastSet
                        else
                            TimerEvent.MoveToPhase(phase)
                    }
                }
            }
            current != null -> {
                // TODO move out somewhere
                val deltaT = action.nanos - lastTime
                val percentProgress =
                    deltaT.toFloat() / current.maxNanos.toFloat()

                val newProgress = progress + percentProgress

                val lastSecond =
                    ((current.maxNanos / 1_000_000_000L) * (1f - progress))
                        .let(::floor)
                        .toInt()
                val curSecond =
                    ((current.maxNanos / 1_000_000_000L) * (1f - newProgress))
                        .let(::floor)
                        .toInt()

                val event = if (lastSecond != curSecond && curSecond in (0..2)) {
                    TimerEvent.SecondsRemaining(curSecond + 1)
                } else null


                copy(
                    progress = newProgress,
                    lastTime = action.nanos
                ) to event
            }
            else -> this to null
        }
    }
    is TimerAction.PlayPause -> copy(lastTime = if (isPaused) System.nanoTime() else null).let {
        it to if (it.isPaused) TimerEvent.Pause else TimerEvent.Resume
    }
    is TimerAction.Stop -> EditState(phases) to TimerEvent.Stop
    else -> this to null
}

val Int.nanos: Long
    get() = this * 1_000_000_000L
