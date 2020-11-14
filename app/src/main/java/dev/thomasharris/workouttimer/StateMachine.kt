package dev.thomasharris.workouttimer

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

class StateMachine<Action, State, Event>(
    initialState: State,
    private val coroutineContext: CoroutineContext = Dispatchers.Main,
    private val reducer: suspend (state: State, action: Action) -> Pair<State, Event?>,
) {

    private val _stateFlow = MutableStateFlow(initialState)
    val stateFlow = _stateFlow.asStateFlow()

    private val _eventFlow = Channel<Event>(capacity = 1)
    val eventFlow = _eventFlow.receiveAsFlow()

    suspend fun accept(action: Action): Pair<State, Event?> = withContext(coroutineContext) {
        reducer(_stateFlow.value, action).also { (newState, newEvent) ->
            _stateFlow.value = newState
            if (newEvent != null)
                _eventFlow.send(newEvent)
        }
    }
}
