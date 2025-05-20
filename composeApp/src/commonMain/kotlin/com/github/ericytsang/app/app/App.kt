package com.github.ericytsang.app.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    viewModel:AppViewModel = AppViewModel.create(),
    logViewerViewModel:LogViewerViewModel = LogViewerViewModel.createDefault(),
    workingFileSetEditorViewModel:WorkingFileSetEditorViewModel = WorkingFileSetEditorViewModel.createDefault(),
)
{
    val theme by viewModel.theme.collectAsState(Theme.DARK)
    val animatedThemeColors by animatedThemeColors(theme)

    var showContent by remember { mutableStateOf(false) }

    var logcatFilterTextFieldState by remember { mutableStateOf("") }
    logViewerViewModel.setLogcatFilterString(logcatFilterTextFieldState)

    val workingFileSet by workingFileSetEditorViewModel.workingFileSet.collectAsState(WorkingFileSetEmpty)
    logViewerViewModel.setConcatenatedFiles(workingFileSet.files.map { File(it.filePath) })

    fillMaxBackground(colors = animatedThemeColors)
    {

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        )
        {
            Button(
                onClick = { showContent = !showContent },
                content = { Text("Click me!") },
            )

            Button(
                onClick = { viewModel.switchTheme() },
                content = { Text("Toggle theme") },
            )

            Button(
                onClick =
                {
                    openWorkingFileSetEditorInNewWindowBlocking(
                        owner = window,
                        viewModel = workingFileSetEditorViewModel,
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
                value = logcatFilterTextFieldState,
                onValueChange = { newValue -> logcatFilterTextFieldState = newValue },
            )

            val logLines by logViewerViewModel.getLogLinesFlow().collectAsState(emptyList())

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
            )
            {
                items(count = logLines.size)
                { index ->
                    val logLineString = logLines[index]
                    Text(
                        text = logLineString,
                        modifier = Modifier.fillMaxWidth(),
                        color = animatedThemeColors.onBackground,
                    )
                }
            }
        }
    }
}

