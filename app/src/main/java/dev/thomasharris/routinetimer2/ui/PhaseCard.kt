package dev.thomasharris.routinetimer2.ui

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowLeft
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.VectorAsset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.routinetimer2.MainViewModel
import dev.thomasharris.routinetimer2.MainViewState
import dev.thomasharris.routinetimer2.Phase

@Composable
fun PhaseCard(
    state: MainViewState,
    phase: Phase,
    onClick: (PhaseCardEvent) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = 4.dp,
    ) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                phase.displayName,
                style = MaterialTheme.typography.h4,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                IconButton(onClick = { onClick(PhaseCardEvent.DECREMENT) }) {
                    Icon(asset = Icons.Default.ArrowLeft.scale(2f))
                }
                Text(
                    state.valueOfFormatted(phase = phase),
                    style = MaterialTheme.typography.body1.copy(fontSize = 32.sp),
                    modifier = Modifier.padding(8.dp)
                )
                IconButton(onClick = { onClick(PhaseCardEvent.INCREMENT) }) {
                    Icon(asset = Icons.Default.ArrowRight.scale(2f))
                }
            }
        }
    }
}

@Composable
@Preview
fun PhaseCardPreview() {
    val state = MainViewModel().stateFlow.value
    PhaseCard(state = state, phase = Phase.PREP, onClick = {})
}

enum class PhaseCardEvent {
    DECREMENT,
    INCREMENT,
}

fun VectorAsset.scale(scale: Float): VectorAsset =
    copy(defaultWidth = defaultWidth * scale, defaultHeight = defaultHeight * scale)

val Phase.displayName: String
    get() = when (this) {
        Phase.PREP -> "Preparation"
        Phase.WORK -> "Work"
        Phase.REST -> "Rest"
        Phase.SETS -> "Sets"
    }

fun MainViewState.valueOfFormatted(phase: Phase): String {
    val n = when (phase) {
        Phase.PREP -> phases.prepTimeSeconds
        Phase.WORK -> phases.workTimeSeconds
        Phase.REST -> phases.restTimeSeconds
        Phase.SETS -> phases.sets
    }

    if (phase == Phase.SETS)
        return n.toString()

    return n.asTime()
}

fun Int.asTime(): String {
    val minutes = this / 60
    val seconds = this % 60
    return "%d:%02d".format(minutes, seconds)
}