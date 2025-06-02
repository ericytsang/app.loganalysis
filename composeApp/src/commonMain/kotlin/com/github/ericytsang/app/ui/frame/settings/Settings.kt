package com.github.ericytsang.app.ui.frame.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.util.component.fillMaxBackground
import kotlinx.coroutines.CoroutineScope

@Composable
fun Settings(
    viewModelFactory:(CoroutineScope)->SettingsViewModel = { uiScope -> SettingsViewModel.create(uiScope) },
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(coroutineScope) }

    val delimiterCharactersText by viewModel.delimiterCharactersText.collectAsState("")
    val delimiterCharactersEnabled by viewModel.delimiterCharactersEnabled.collectAsState(false)

    fillMaxBackground()
    { themeColors ->

        Column(
            modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
            horizontalAlignment = Alignment.Start,
        )
        {
            Button(
                modifier = Modifier.padding(bottom = Dimens.mttPadding),
                onClick = { viewModel.switchTheme() },
                content = { Text("Toggle theme") },
            )

            TextField(
                value = delimiterCharactersText,
                enabled = delimiterCharactersEnabled,
                onValueChange = { newValue -> viewModel.setDelimiterCharacters(newValue) },
                label = { Text("delimiters") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = themeColors.onBackground,
                ),
            )
        }
    }
}