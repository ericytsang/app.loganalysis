package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.util.fillMaxBackground

@Composable
fun ProjectBrowserWindow(
    rootChildWindowManager:ChildWindowManager,
    controller:ChildWindowManagerController,
)
{
    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "Project Browser",
    )
    {
        fillMaxBackground()
        { themeColors ->
            ProjectBrowser(
                themeColors = themeColors,
                viewModelFactory = { ProjectBrowserViewModelImpl() },
                rootChildWindowManager = rootChildWindowManager,
            )
        }
    }
}
