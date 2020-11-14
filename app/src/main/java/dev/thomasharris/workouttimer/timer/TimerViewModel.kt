package dev.thomasharris.workouttimer.timer

import dev.thomasharris.workouttimer.util.WakeLocker
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class TimerViewModel(
    private val wakeLocker: WakeLocker,
) {

    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null


    private val _stateFlow: MutableStateFlow<TimerViewState> = MutableStateFlow(DEFAULT_STATE)
    val stateFlow = _stateFlow.asStateFlow()

    private val _eventFlow = Channel<Event>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private fun dispatchFrame(nanos: Long) {
        _stateFlow.value = _stateFlow.value.accept(Action.Frame(nanos)).handle()
    }

    fun onToggle() {
        _stateFlow.value = _stateFlow.value.accept(Action.PlayPause).handle()
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        _stateFlow.value = when (phaseCardEvent) {
            PhaseCardEvent.INCREMENT -> _stateFlow.value.accept(Action.Increment(phase))
            PhaseCardEvent.DECREMENT -> _stateFlow.value.accept(Action.Decrement(phase))
        }.handle()
    }

    fun onStopClicked() {
        _stateFlow.value = _stateFlow.value.accept(Action.Stop).handle()
    }

    private fun Pair<TimerViewState, Event?>.handle(): TimerViewState {
        second?.let { event ->
            // TODO restructure handle() to update state then handle events,
            //  because events could cause further state updates!
            when (event) {
                is Event.Pause, Event.Done, Event.Stop -> {
                    job?.cancel()
                    job = null
                }
                is Event.Resume, Event.Start -> {
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

            scope.launch {
                _eventFlow.send(event)
            }
        }

        return first
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