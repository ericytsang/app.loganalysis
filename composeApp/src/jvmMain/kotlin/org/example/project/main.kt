package org.example.project

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.github.ericytsang.app.app.App
import com.github.ericytsang.app.app.AppViewModel
import com.github.ericytsang.app.app.LogViewerViewModel
import com.github.ericytsang.app.app.WorkingFileSetEditorViewModel

fun main() = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "Log Analyzer",
    )
    {

        App(
            window = window,
            viewModel = AppViewModel.create(),
            logViewerViewModel = LogViewerViewModel.createDefault(),
            workingFileSetEditorViewModel = WorkingFileSetEditorViewModel.createDefault(),
        )
    }
}
