package dev.thomasharris.routinetimer2

import dev.thomasharris.routinetimer2.ui.PhaseCardEvent
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


    fun onToggle() {
        _stateFlow.value = when (val v = stateFlow.value) {
            is EditState -> InProgressState(v.phases, Phase.PREP, 0.25f, false)
            is InProgressState -> EditState(v.phases)
        }
    }

    fun onPhaseClicked(phase: Phase, phaseCardEvent: PhaseCardEvent) {
        when (val state = _stateFlow.value) {
            is EditState -> {
                val increment = when (phaseCardEvent) {
                    PhaseCardEvent.INCREMENT -> if (phase == Phase.SETS) 1 else 5
                    PhaseCardEvent.DECREMENT -> if (phase == Phase.SETS) -1 else -5
                }

                // TODO ew
                val newPhases = when (phase) {
                    Phase.PREP -> if (state.phases.prepTimeSeconds + increment >= 5)
                        state.phases.copy(prepTimeSeconds = state.phases.prepTimeSeconds + increment)
                    else
                        state.phases
                    Phase.WORK -> if (state.phases.workTimeSeconds + increment >= 5)
                        state.phases.copy(workTimeSeconds = state.phases.workTimeSeconds + increment)
                    else
                        state.phases

                    Phase.REST -> if (state.phases.restTimeSeconds + increment >= 5)
                        state.phases.copy(restTimeSeconds = state.phases.restTimeSeconds + increment)
                    else
                        state.phases

                    Phase.SETS -> if (state.phases.sets + increment >= 1)
                        state.phases.copy(sets = state.phases.sets + increment)
                    else
                        state.phases
                }

                _stateFlow.value = state.copy(phases = newPhases)
            }
            else -> {
                // nothing
            }
        }
    }
}