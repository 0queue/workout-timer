package dev.thomasharris.workouttimer.timer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

// todo could be a class and some default or extension methods
abstract class StateMachine<Action, State, Event>(
    initialState: State,
    private val coroutineContext: CoroutineContext = Dispatchers.Main,
) {

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow = _stateFlow.asStateFlow()

    private val _eventFlow = Channel<Event>(capacity = 1)
    val eventFlow = _eventFlow.receiveAsFlow()

    suspend fun accept(action: Action): Output<State, Event> = withContext(coroutineContext) {
        reduce(_stateFlow.value, action).also { (newState, newEvent) ->
            _stateFlow.value = newState
            if (newEvent != null)
                _eventFlow.send(newEvent)
        }
    }

    protected abstract suspend fun reduce(state: State, action: Action): Output<State, Event>

    protected infix fun State.and(event: Event?): Output<State, Event> = Output(this, event)

    protected fun Pair<State, Event?>.toOutput() = Output(first, second)

    data class Output<State, Event>(
        val state: State,
        val event: Event?,
    )
}

class TimerStateMachine : StateMachine<Action, TimerViewState, Event>(
    TimerViewModel.DEFAULT_STATE
) {
    override suspend fun reduce(
        state: TimerViewState,
        action: Action,
    ): Output<TimerViewState, Event> = state.accept(action).toOutput()
}