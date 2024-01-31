package com.cornmuffin.prototype.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.cornmuffin.prototype.data.settings.Setting

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    settings: List<Setting>,
    modifier: Modifier = Modifier,
) {
    val viewModel = hiltViewModel<SettingsViewModel>()

    Column {
        TopAppBar(
            title = {
                Text("Settings")
            },
            navigationIcon = {
                IconButton(
                    onClick = { viewModel.reduceViewModel(SettingsViewModel.Action.GoBack) }
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            }
        )

        LazyColumn(modifier = modifier) {
            items(settings) {
                Setting(it)
                Divider()
            }
        }
    }
}

@Composable
@Preview(showBackground = true, backgroundColor = 0xFFFFFF)
fun SettingsPreview() {
    Settings(
        listOf(
            Setting.BinarySetting(name = "Enable Debugging", value = true),
            Setting.BinarySetting(name = "Track Analytics", value = false),
        ),
    )
}

@Composable
private fun Setting(setting: Setting) {
    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row {
            Text(
                fontWeight = FontWeight.Bold,
                text = setting.name,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = setting.displayableValue
            )
        }
    }
}
