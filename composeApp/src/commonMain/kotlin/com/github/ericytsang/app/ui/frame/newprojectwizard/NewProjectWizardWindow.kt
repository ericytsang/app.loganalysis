package com.github.ericytsang.app.ui.frame.newprojectwizard

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.util.fillMaxBackground

@Composable
fun NewProjectWizardWindow(
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
            NewProjectWizard(
                window = window,
                themeColors = themeColors,
                rootChildWindowManager = rootChildWindowManager,
            )
        }
    }
}