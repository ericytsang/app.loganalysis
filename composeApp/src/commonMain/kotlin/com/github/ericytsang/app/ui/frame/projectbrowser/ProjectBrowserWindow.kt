package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManager
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.app.util.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope

@Composable
fun ProjectBrowserWindow(
    rootChildWindowManager:ChildWindowManager,
    controller:ChildWindowManagerController,
)
{
    val coroutineScope = rememberCoroutineScope().asImmutableCoroutineScope()

    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "Project Browser",
    )
    {
        // bring the window to focus when requested by the child window manager
        BringWindowToFocusOnRequest(
            uiScope = coroutineScope,
            window = window,
            controller = controller,
        )

        // window content
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
