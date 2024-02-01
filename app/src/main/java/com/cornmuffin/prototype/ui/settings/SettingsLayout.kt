package com.cornmuffin.prototype.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.cornmuffin.prototype.R
import com.cornmuffin.prototype.data.settings.Settings
import com.cornmuffin.prototype.ui.common.CommonTopAppBar

@Composable
fun SettingsLayout() {
    val settingsViewModelState by hiltViewModel<SettingsViewModel>().stateFlow().collectAsState()

    when (settingsViewModelState.state) {
        SettingsViewModelState.State.LOADING -> Loading()
        SettingsViewModelState.State.SUCCESSFUL -> Settings(settingsViewModelState.settings)
        else -> Text("Unexpected settings state: ${settingsViewModelState.state}")
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun Loading(text: String = stringResource(R.string.loading)) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                textAlign = TextAlign.Center,
                text = text,
            )

            CircularProgressIndicator(
                modifier = Modifier.width(32.dp),
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
fun Settings(settings: Settings) {
    val viewModel = hiltViewModel<SettingsViewModel>()

    Column {
        CommonTopAppBar {
            viewModel.goBack()
        }

        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {

            Row {
                Text(
                    fontWeight = FontWeight.Bold,
                    text = "Enable Debugging",
                )

                Spacer(modifier = Modifier.weight(1f))

                Checkbox(
                    checked = settings.enableDebugging,
                    onCheckedChange = { newValue ->
                        viewModel.reduceViewModel(
                            SettingsViewModel.Action.UpdateDisk(
                                settings.copy(
                                    enableDebugging = newValue
                                )
                            )
                        )
                    }
                )
            }
        }
    }
}
