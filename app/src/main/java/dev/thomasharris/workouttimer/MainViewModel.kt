package dev.thomasharris.workouttimer

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import dev.thomasharris.workouttimer.ui.PhaseCardEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.util.concurrent.CountDownLatch

class MainViewModel {

    private val mainChoreographer: Choreographer

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        // taken from AndroidAnimationClock
        // TODO move away from choreographer, it really only is for ui
        //   Although I want nice animations on the progress bar, I also
        //   want the timer to run in the background, or with screen off
        //   This actually works in the emulator but not on my own device,
        //   and most likely not others as well
        if (Looper.myLooper() == Looper.getMainLooper()) {
            mainChoreographer = Choreographer.getInstance()
        } else {
            val latch = CountDownLatch(1)
            var choreographer: Choreographer? = null
            Handler(Looper.getMainLooper()).postAtFrontOfQueue {
                try {
                    choreographer = Choreographer.getInstance()
                } finally {
                    latch.countDown()
                }
            }
            latch.await()
            mainChoreographer = choreographer!!
        }
    }

    private val _stateFlow: MutableStateFlow<MainViewState> = MutableStateFlow(EditState(Phases(
        prepTimeSeconds = 5,
        workTimeSeconds = 30,
        restTimeSeconds = 5,
        sets = 3,
    )))
    val stateFlow = _stateFlow.asStateFlow()

    private val _eventFlow = Channel<Event>()
    val eventFlow = _eventFlow.receiveAsFlow()

    private val frameCallback: Choreographer.FrameCallback =
        Choreographer.FrameCallback(this::dispatchFrame)

    private fun dispatchFrame(nanos: Long) {
        _stateFlow.value = _stateFlow.value.accept(Action.Frame(nanos)).let { (state, event) ->
            if (state is InProgressState && !state.isPaused)
                mainChoreographer.postFrameCallback(frameCallback)

            if (event != null) scope.launch {
                _eventFlow.send(event)
            }

            state
        }
    }

    fun onToggle() {
        _stateFlow.value = _stateFlow.value.accept(Action.PlayPause).let { (state, event) ->
            if (state is InProgressState && !state.isPaused) {
                mainChoreographer.postFrameCallback(frameCallback)
            }

            if (event != null) scope.launch {
                _eventFlow.send(event)
            }

            state
        }
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        _stateFlow.value = when (phaseCardEvent) {
            PhaseCardEvent.INCREMENT -> _stateFlow.value.accept(Action.Increment(phase))
            PhaseCardEvent.DECREMENT -> _stateFlow.value.accept(Action.Decrement(phase))
        }.let { (state, event) ->
            if (event != null) scope.launch {
                _eventFlow.send(event)
            }

            state
        }
    }

    fun onStopClicked() {
        _stateFlow.value = _stateFlow.value.accept(Action.Stop).let { (state, event) ->
            if (event != null) scope.launch {
                _eventFlow.send(event)
            }

            state
        }

    }
}