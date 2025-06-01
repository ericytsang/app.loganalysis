package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.domain.objects.ConfigurationId

@Composable
fun LogViewerRootWindow(
    configurationId:ConfigurationId,
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
                workingFileSetEditorViewModelFactory = { WorkingFileSetEditorViewModel.create(configurationId) },
            )
        }
    }
}

