package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Colors
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
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
import androidx.compose.ui.awt.ComposeWindow
import com.github.ericytsang.app.model.Dimens
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.asset.IconMatchCase
import com.github.ericytsang.app.ui.util.asset.IconWrapText
import com.github.ericytsang.app.ui.util.component.ColorCodedLogLine
import com.github.ericytsang.app.ui.util.component.CommonWindowHeader
import com.github.ericytsang.app.ui.util.component.ToggleButton
import com.github.ericytsang.app.ui.util.openNewProjectWizard
import com.github.ericytsang.app.ui.util.openProjectBrowser
import com.github.ericytsang.domain.objects.Theme
import com.github.ericytsang.domain.objects.WorkingFileSetEmpty
import kotlinx.coroutines.CoroutineScope
import java.io.File

@Composable
fun LogViewerRoot(
    window:ComposeWindow,
    themeColors: Colors,
    rootChildWindowManager:ChildWindowManager,
    workingFileSetEditorViewModelFactory:()->WorkingFileSetEditorViewModel,
    viewModelFactory:(CoroutineScope)->LogViewerRootViewModel = { uiScope -> LogViewerRootViewModel.create(uiScope) },
    logViewerViewModelFactory:()->LogViewerViewModel = { LogViewerViewModel.createDefault() },
)
{
    val uiScope = rememberCoroutineScope()
    val viewModel = remember { viewModelFactory(uiScope) }
    val logViewerViewModel = remember { logViewerViewModelFactory() }
    val workingFileSetEditorViewModel = remember { workingFileSetEditorViewModelFactory() }

    val theme by viewModel.theme.collectAsState(Theme.DARK)

    val logcatFilterString by logViewerViewModel.logcatFilterString.collectAsState("")
    val delimiters by viewModel.getDelimiterFlow().collectAsState("")
    val shouldMatchCase by logViewerViewModel.isCaseSensitive.collectAsState(false)

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

    val shouldWrapText by viewModel.getWordWrapFlow().collectAsState(false)

    Column(
        modifier = Modifier.fillMaxSize().padding(Dimens.mttPadding),
        verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
    )
    {
        CommonWindowHeader(themeColors,rootChildWindowManager)
        {
            // new project button
            Button(
                onClick = { rootChildWindowManager.openNewProjectWizard() },
                content = { Text("New project") },
                colors = ButtonDefaults.buttonColors(themeColors.surface),
            )

            // new project button
            Button(
                onClick = { rootChildWindowManager.openProjectBrowser() },
                content = { Text("Recent projects") },
                colors = ButtonDefaults.buttonColors(themeColors.surface),
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
        )
        {
            // logcat filter input and log viewer
            Column(
                modifier = Modifier.weight(1f, fill = true),
                verticalArrangement = Arrangement.spacedBy(Dimens.mttPadding),
            )
            {

                // logcat filter input
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    border = ButtonDefaults.outlinedBorder,
                )
                {
                    Row(
                        modifier = Modifier.padding(Dimens.mttPadding),
                        verticalAlignment = Alignment.CenterVertically,
                    )
                    {

                        // user input for entering the logcat filter string
                        TextField(
                            value = logcatFilterString,
                            onValueChange = { newValue -> logViewerViewModel.setLogcatFilterString(newValue) },
                            modifier = Modifier.weight(1f).padding(end = Dimens.mttPadding),
                            colors = TextFieldDefaults.textFieldColors(
                                textColor = themeColors.onBackground,
                            ),
                        )

                        // toggle button for enabling/disabling case-sensitive matching in the logcat filter
                        ToggleButton(
                            modifier = Modifier.padding(end = Dimens.mttPadding),
                            onClick = { logViewerViewModel.setCaseSensitive(!logViewerViewModel.isCaseSensitive.value) },
                            isToggled = shouldMatchCase,
                            isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                            isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                            content = { contentColor -> IconMatchCase(contentColor) },
                        )

                        // toggle button for enabling/disabling word wrapping in the log viewer
                        ToggleButton(
                            modifier = Modifier.padding(end = Dimens.mttPadding),
                            onClick = { viewModel.toggleWordWrap() },
                            isToggled = shouldWrapText,
                            isToggledColors = ButtonDefaults.buttonColors(themeColors.primary),
                            isNotToggledColors = ButtonDefaults.buttonColors(themeColors.surface),
                            content = { contentColor -> IconWrapText(contentColor) },
                        )
                    }
                }

                // region log viewer

                val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = MaterialTheme.shapes.small,
                    border = ButtonDefaults.outlinedBorder,
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
                                defaultColor = themeColors.onBackground,
                                softWrap = shouldWrapText,
                            )
                        }
                    }
                }

                // endregion
            }

            // filter helpers and working file set editor
            Column()
            {
                Surface(
                    modifier = Modifier.fillMaxHeight(),
                    shape = MaterialTheme.shapes.small,
                    border = ButtonDefaults.outlinedBorder,
                )
                {
                    Text("placeholder")
                }
            }
        }
    }
}