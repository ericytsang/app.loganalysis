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
import com.github.ericytsang.app.ui.frame.settings.SettingsViewModel
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import kotlinx.coroutines.CoroutineScope

@Composable
fun Settings(
    viewModelFactory:(CoroutineScope)->SettingsViewModel = { uiScope -> SettingsViewModel.Companion.create(uiScope) },
)
{
    val coroutineScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(coroutineScope) }

    val theme by viewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    val delimiterCharacters by viewModel.delimiterCharacters.collectAsState("")

    fillMaxBackground(colors = animatedThemeColors)
    {

        Column(
            modifier = Modifier.Companion.fillMaxWidth().padding(Dimens.mttPadding),
            horizontalAlignment = Alignment.Companion.Start,
        )
        {
            Button(
                modifier = Modifier.Companion.padding(bottom = Dimens.mttPadding),
                onClick = { viewModel.switchTheme() },
                content = { Text("Toggle theme") },
            )

            TextField(
                value = delimiterCharacters,
                onValueChange = { newValue -> viewModel.setDelimiterCharacters(newValue) },
                label = { Text("delimiters") },
                colors = TextFieldDefaults.textFieldColors(
                    textColor = animatedThemeColors.onBackground,
                ),
            )
        }
    }
}