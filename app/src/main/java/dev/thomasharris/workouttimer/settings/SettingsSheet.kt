package dev.thomasharris.workouttimer.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.workouttimer.BuildConfig
import dev.thomasharris.workouttimer.R
import dev.thomasharris.workouttimer.ui.NightModeRadioButton
import dev.thomasharris.workouttimer.ui.theme.WorkoutTimerTheme

@Composable
fun SettingsSheet(
    settingsViewState: SettingsViewState,
    onNightModeSelected: (SettingsViewState.NightMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(8.dp),
            style = MaterialTheme.typography.h5,
            text = "Settings",
        )

        val debugText = if (BuildConfig.BUILD_TYPE == "debug") " (debug)" else ""
        val caption =
            "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_CODE}${debugText}"
        Text(
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = MaterialTheme.typography.caption.copy(fontStyle = FontStyle.Italic),
            text = caption,
        )

        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            SettingsViewState.NightMode.values().forEach {
                NightModeRadioButton(
                    settingsViewState = settingsViewState,
                    nightMode = it,
                    onNightModeSelected = onNightModeSelected
                )
            }
        }

        GithubLinkText(
            modifier = Modifier.align(Alignment.CenterHorizontally).padding(16.dp)
        )
    }
}

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
