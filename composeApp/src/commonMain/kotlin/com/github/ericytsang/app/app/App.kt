package com.github.ericytsang.app.app

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Colors
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
import androidx.compose.ui.graphics.ColorFilter
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.modal.openSettingsInNewWindowBlocking
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinproject.composeapp.generated.resources.Res
import kotlinproject.composeapp.generated.resources.compose_multiplatform
import kotlinproject.composeapp.generated.resources.outline_match_case_24
import kotlinproject.composeapp.generated.resources.outline_settings_24
import kotlinproject.composeapp.generated.resources.outline_wrap_text_24
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window
import java.io.File

@Composable
@Preview
fun App(
    window:Window,
    viewModelFactory:()->AppViewModel = { AppViewModel.create() },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.createDefault() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel = { WorkingFileSetEditorViewModel.createDefault() },
)
{
    val viewModel = remember { viewModelFactory() }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    val logcatFilterString by logViewerViewModel.logcatFilterString.collectAsState("")
    val delimiters by viewModel.getDelimiterFlow().collectAsState("")

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

    fillMaxBackground(colors = animatedThemeColors)
    {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Row(
                modifier = Modifier.fillMaxWidth().padding(Dimens.mttPadding),
                verticalAlignment = Alignment.CenterVertically,
            )
            {
                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick =
                        {
                            openWorkingFileSetEditorInNewWindowBlocking(
                                owner = window,
                                viewModel = workingFileSetEditorViewModel,
                                appViewModel = viewModel,
                            )
                        },
                    content =
                        {
                            val workingFileSetFiles = workingFileSet.files
                            if (workingFileSetFiles.isEmpty())
                            {
                                Text("Add files")
                            }
                            else
                            {
                                Text("Edit files (${workingFileSetFiles.size})")
                            }
                        },
                )

                TextField(
                    value = logcatFilterString,
                    onValueChange = { newValue -> logViewerViewModel.setLogcatFilterString(newValue) },
                    modifier = Modifier.weight(1f).padding(end = Dimens.mttPadding),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = animatedThemeColors.onBackground,
                    ),
                )

                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick = { openSettingsInNewWindowBlocking(window) },
                    content = { IconMatchCaseOnPrimary(animatedThemeColors) },
                )

                Button(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick = { openSettingsInNewWindowBlocking(window) },
                    content = { IconWrapTextOnPrimary(animatedThemeColors) },
                )

                Button(
                    onClick = { openSettingsInNewWindowBlocking(window) },
                    content = { IconSettingsOnPrimary(animatedThemeColors) },
                )
            }

            val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

            Column(
                modifier = Modifier.fillMaxSize().padding(Dimens.mttPadding),
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                )
                {
                    items(count = logLines.size)
                    { index ->
                        ColorCodedLogLine(
                            text = logLines[index],
                            modifier = Modifier.fillMaxWidth(),
                            delimiters = delimiters,
                            themeForColorCoding = theme,
                            defaultColor = animatedThemeColors.onBackground,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun IconSettingsOnPrimary(animatedThemeColors:Colors)
{
    Image(
        painter = painterResource(Res.drawable.outline_settings_24),
        contentDescription = "Settings",
        colorFilter = ColorFilter.lighting(
            multiply = animatedThemeColors.onPrimary,
            add = animatedThemeColors.onPrimary,
        )
    )
}

@Composable
private fun IconWrapTextOnPrimary(animatedThemeColors:Colors)
{
    Image(
        painter = painterResource(Res.drawable.outline_wrap_text_24),
        contentDescription = "Wrap text",
        colorFilter = ColorFilter.lighting(
            multiply = animatedThemeColors.onPrimary,
            add = animatedThemeColors.onPrimary,
        )
    )
}

@Composable
private fun IconMatchCaseOnPrimary(animatedThemeColors:Colors)
{
    Image(
        painter = painterResource(Res.drawable.outline_match_case_24),
        contentDescription = "Match case",
        colorFilter = ColorFilter.lighting(
            multiply = animatedThemeColors.onPrimary,
            add = animatedThemeColors.onPrimary,
        )
    )
}

@Composable
fun Settings(
    viewModelFactory:(CoroutineScope)->SettingsViewModel = { uiScope -> SettingsViewModel.create(uiScope) },
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
