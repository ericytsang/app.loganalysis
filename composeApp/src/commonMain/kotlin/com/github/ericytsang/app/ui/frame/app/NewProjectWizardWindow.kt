package com.github.ericytsang.app.ui.frame.app

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.rememberWindowState
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.ui.frame.workingfileseteditor.NewProjectWizard
import com.github.ericytsang.app.util.fillMaxBackground

@Composable
fun NewProjectWizardWindow(
    controller:ChildWindowManagerController,
)
{
    val windowState = rememberWindowState()
    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "New Project",
        state = windowState,
    )
    {
        fillMaxBackground()
        { themeColors ->
            NewProjectWizard(
                window = window,
                themeColors = themeColors,
            )
        }
    }
}