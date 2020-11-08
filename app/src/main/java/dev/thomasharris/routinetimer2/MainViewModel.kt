package dev.thomasharris.routinetimer2

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import dev.thomasharris.routinetimer2.ui.PhaseCardEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.replay
import java.util.concurrent.CountDownLatch

class MainViewModel {

    private val scope = CoroutineScope(Dispatchers.Default)

    private val mainChoreographer: Choreographer

    init {
        // taken from AndroidAnimationClock
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
        workTimeSeconds = 5,
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

            if (event != null)
                _eventFlow.sendBlocking(event)

            state
        }
    }

    fun onToggle() {
        _stateFlow.value = _stateFlow.value.accept(Action.PlayPause).let { (state, event) ->
            if (state is InProgressState && !state.isPaused) {
                mainChoreographer.postFrameCallback(frameCallback)
            }

            if (event != null)
                _eventFlow.sendBlocking(event)

            state
        }
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        _stateFlow.value = when (phaseCardEvent) {
            PhaseCardEvent.INCREMENT -> _stateFlow.value.accept(Action.Increment(phase))
            PhaseCardEvent.DECREMENT -> _stateFlow.value.accept(Action.Decrement(phase))
        }.let { (state, event) ->
            if (event != null)
                _eventFlow.sendBlocking(event)

            state
        }
    }

    fun onStopClicked() {
        _stateFlow.value = _stateFlow.value.accept(Action.Stop).let { (state, event) ->
            if (event != null)
                _eventFlow.sendBlocking(event)

            state
        }

    }
}