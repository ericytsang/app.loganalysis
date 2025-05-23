package com.github.ericytsang.app.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconEditLogFilesOnPrimary
import com.github.ericytsang.app.ui.asset.IconMatchCaseOnPrimary
import com.github.ericytsang.app.ui.asset.IconSettingsOnPrimary
import com.github.ericytsang.app.ui.asset.IconWrapTextOnPrimary
import com.github.ericytsang.app.ui.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.modal.openSettingsInNewWindowBlocking
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
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
                    content = { IconEditLogFilesOnPrimary(animatedThemeColors) },
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

