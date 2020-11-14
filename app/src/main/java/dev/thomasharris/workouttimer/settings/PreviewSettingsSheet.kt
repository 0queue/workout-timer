package dev.thomasharris.workouttimer.settings

import androidx.compose.runtime.Composable
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme

@Preview(showBackground = true)
@Composable
fun PreviewSettingsSheet() {
    WorkoutTimerTheme {
        SettingsSheet(
            settingsViewState = SettingsViewState(nightMode = SettingsViewState.NightMode.System),
            onNightModeSelected = {},
        )
    }
}