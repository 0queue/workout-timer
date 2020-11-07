package dev.thomasharris.routinetimer2

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Text
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.lifecycle.lifecycleScope
import androidx.ui.tooling.preview.Preview
import dev.thomasharris.routinetimer2.ui.RoutineTimer2Theme
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        lifecycleScope.launch {
            mainViewModel.stateFlow.collect {

            }
        }


        setContent {
            val state by mainViewModel.stateFlow.collectAsState()

            RoutineTimer2Theme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    EditScreen(state = state, onClick = mainViewModel::toggle)
                }
            }
        }
    }
}

@Composable
fun Debug(state: MainViewState) {
    Text(text = "state: $state")
}

@Composable
fun EditScreen(
    state: MainViewState,
    onClick: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Debug(state)
        Button(onClick = onClick) {
            Text("Toggle me")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    RoutineTimer2Theme {
        EditScreen(state = MainViewModel().stateFlow.value, onClick = {})
    }
}