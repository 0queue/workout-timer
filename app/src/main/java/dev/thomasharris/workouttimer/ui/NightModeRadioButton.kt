package dev.thomasharris.workouttimer.ui

import androidx.compose.foundation.InteractionState
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.thomasharris.workouttimer.settings.SettingsViewState

@Composable
fun NightModeRadioButton(
    settingsViewState: SettingsViewState,
    nightMode: SettingsViewState.NightMode,
    onNightModeSelected: (SettingsViewState.NightMode) -> Unit,
) {
    val interactionState = remember { InteractionState() }
    Row {
        RadioButton(
            interactionState = interactionState,
            modifier = Modifier
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            selected = settingsViewState.nightMode == nightMode,
            enabled = settingsViewState.nightMode != null,
            onClick = { onNightModeSelected(nightMode) }
        )
        Text(
            modifier = Modifier
                .clickable(
                    interactionState = interactionState,
                    indication = null,
                    onClick = { onNightModeSelected(nightMode) },
                    enabled = settingsViewState.nightMode != null,
                )
                .padding(4.dp)
                .align(Alignment.CenterVertically),
            text = nightMode.toString(),
        )
    }
}