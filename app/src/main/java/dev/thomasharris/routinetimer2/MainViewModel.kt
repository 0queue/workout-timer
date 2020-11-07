package dev.thomasharris.routinetimer2

import android.os.Handler
import android.os.Looper
import android.view.Choreographer
import dev.thomasharris.routinetimer2.ui.PhaseCardEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    private val frameCallback: Choreographer.FrameCallback = Choreographer.FrameCallback(this::dispatchFrame)

    private fun dispatchFrame(nanos: Long) {
        _stateFlow.value = _stateFlow.value.accept(Action.Frame(nanos)).also { state ->
            if (state is InProgressState && !state.isPaused)
                mainChoreographer.postFrameCallback(frameCallback)
        }
    }

    fun onToggle() {
        _stateFlow.value = _stateFlow.value.accept(Action.PlayPause).also { state ->
            if (state is InProgressState && !state.isPaused) {
                mainChoreographer.postFrameCallback(frameCallback)
            }
        }
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        _stateFlow.value = when (phaseCardEvent) {
            PhaseCardEvent.INCREMENT -> _stateFlow.value.accept(Action.Increment(phase))
            PhaseCardEvent.DECREMENT -> _stateFlow.value.accept(Action.Decrement(phase))
        }
    }

    fun onStopClicked() {
        _stateFlow.value = _stateFlow.value.accept(Action.Stop)
    }
}