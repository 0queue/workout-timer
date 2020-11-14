package dev.thomasharris.workouttimer.settings

import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import dev.thomasharris.workouttimer.BuildConfig
import dev.thomasharris.workouttimer.R
import dev.thomasharris.workouttimer.ui.NightModeRadioButton

@Composable
fun SettingsSheet(
    settingsViewState: SettingsViewState,
    onNightModeSelected: (SettingsViewState.NightMode) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
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
    }
}