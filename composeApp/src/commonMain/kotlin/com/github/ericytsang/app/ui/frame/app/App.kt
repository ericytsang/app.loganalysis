package com.github.ericytsang.app.ui.frame.app

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonColors
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.asset.IconEditLogFilesOnPrimary
import com.github.ericytsang.app.ui.asset.IconMatchCaseOnPrimary
import com.github.ericytsang.app.ui.asset.IconSettingsOnPrimary
import com.github.ericytsang.app.ui.asset.IconWrapTextOnPrimary
import com.github.ericytsang.app.ui.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.modal.openSettingsInNewWindowBlocking
import com.github.ericytsang.app.ui.modal.openWorkingFileSetEditorInNewWindowBlocking
import com.github.ericytsang.app.util.animatedThemeColors
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinx.coroutines.CoroutineScope
import org.jetbrains.compose.ui.tooling.preview.Preview
import java.awt.Window
import java.io.File

@Composable
@Preview
fun App(
    window:Window,
    viewModelFactory:(CoroutineScope)->AppViewModel = { uiScope -> AppViewModel.Companion.create(uiScope) },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.Companion.createDefault() },
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel = { WorkingFileSetEditorViewModel.createDefault() },
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(uiScope) }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    val logcatFilterString by logViewerViewModel.logcatFilterString.collectAsState("")
    val delimiters by viewModel.getDelimiterFlow().collectAsState("")

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

    var shouldMatchCase by remember { mutableStateOf(false) }
    var shouldWrapText by remember { mutableStateOf(false) }

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
                    content = { IconEditLogFilesOnPrimary(animatedThemeColors.onPrimary) },
                )

                TextField(
                    value = logcatFilterString,
                    onValueChange = { newValue -> logViewerViewModel.setLogcatFilterString(newValue) },
                    modifier = Modifier.weight(1f).padding(end = Dimens.mttPadding),
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = animatedThemeColors.onBackground,
                    ),
                )

                ToggleButton(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick = { shouldMatchCase = !shouldMatchCase },
                    isToggled = shouldMatchCase,
                    content = { contentColor -> IconMatchCaseOnPrimary(contentColor) },
                )

                ToggleButton(
                    modifier = Modifier.padding(end = Dimens.mttPadding),
                    onClick = { shouldWrapText = !shouldWrapText },
                    isToggled = shouldWrapText,
                    content = { contentColor -> IconWrapTextOnPrimary(contentColor) },
                )

                Button(
                    onClick = { openSettingsInNewWindowBlocking(window) },
                    content = { IconSettingsOnPrimary(animatedThemeColors.onPrimary) },
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
fun ToggleButton(
    modifier:Modifier,
    onClick:()->Unit,
    isToggled:Boolean,
    isNotToggledColors:ButtonColors = buttonColors(),
    isToggledColors:ButtonColors = buttonColors(MaterialTheme.colors.secondary),
    content:@Composable (contentColor:Color)->Unit,
)
{
    val targetButtonColors = if (isToggled) isToggledColors else isNotToggledColors
    val targetContentColor by targetButtonColors.contentColor(true)
    val contentColor by animateColorAsState(targetContentColor)
    val targetBackgroundColor by targetButtonColors.backgroundColor(true)
    val backgroundColor by animateColorAsState(targetBackgroundColor)
    val colors = buttonColors(
        backgroundColor = backgroundColor,
        contentColor = contentColor,
    )
    Button(
        modifier = modifier,
        onClick = onClick,
        colors = colors,
        content = { content(contentColor) },
    )
}
