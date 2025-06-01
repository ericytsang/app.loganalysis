package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.util.fillMaxBackground

@Composable
fun LogViewerRootWindow(
    rootChildWindowManager:ChildWindowManager,
    controller:ChildWindowManagerController,
)
{
    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "New Project",
    )
    {
        fillMaxBackground()
        { themeColors ->
            LogViewerRoot(
                window = window,
                themeColors = themeColors,
            )
        }
    }
}

