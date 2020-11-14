package dev.thomasharris.workouttimer.timer

import dev.thomasharris.workouttimer.StateMachine
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import dev.thomasharris.workouttimer.util.WakeLocker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class TimerViewModel(
    private val wakeLocker: WakeLocker,
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    private val machine = StateMachine(DEFAULT_STATE, reducer = ::reducer)

    val stateFlow = machine.stateFlow
    val eventFlow = machine.eventFlow.onEach { event ->
        when (event) {
            is TimerEvent.Pause, TimerEvent.Done, TimerEvent.Stop -> {
                job?.cancel()
                job = null
            }
            is TimerEvent.Resume, TimerEvent.Start -> {
                if (job == null) job = scope.launch {
                    try {
                        // stay awake while timing
                        // is cancelled when the user
                        // pauses, stops, or finishes the exercise
                        wakeLocker.lock()
                        while (true) {
                            delay(8)
                            dispatchFrame(System.nanoTime())
                        }
                    } finally {
                        wakeLocker.unlock()
                    }
                }

            }
            else -> Unit // pass
        }
    }

    private suspend fun dispatchFrame(nanos: Long) {
        machine.accept(TimerAction.Frame(nanos))
    }

    fun onToggle() {
        scope.launch {
            machine.accept(TimerAction.PlayPause)
        }
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        scope.launch {
            when (phaseCardEvent) {
                PhaseCardEvent.INCREMENT -> TimerAction.Increment(phase)
                PhaseCardEvent.DECREMENT -> TimerAction.Decrement(phase)
            }.let {
                machine.accept(it)
            }
        }
    }

    fun onStopClicked() {
        scope.launch {
            machine.accept(TimerAction.Stop)
        }
    }

    companion object {
        val DEFAULT_STATE = EditState(Phases(
            prepTimeSeconds = 5,
            workTimeSeconds = 30,
            restTimeSeconds = 5,
            sets = 3,
        ))
    }
}