package dev.thomasharris.routinetimer2

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class MainViewModel {
    private val _stateFlow: MutableStateFlow<MainViewState> = MutableStateFlow(EditState(Phases(
        prepTimeSeconds = 5,
        workTimeSeconds = 5,
        restTimeSeconds = 5,
        sets = 3,
    )))
    val stateFlow = _stateFlow.asStateFlow()


    fun toggle() {
        _stateFlow.value = when (val v = stateFlow.value) {
            is EditState -> InProgressState(v.phases, "prep", 0.25f, false)
            is InProgressState -> EditState(v.phases)
        }
    }
}