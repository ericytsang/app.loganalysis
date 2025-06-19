package com.github.ericytsang.app.ui.frame.projectbrowser

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
import com.github.ericytsang.app.ui.util.usecase.BringWindowToFocusOnRequest
import com.github.ericytsang.app.ui.util.component.fillMaxBackground
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
                viewModelFactory = { ProjectBrowserViewModel.create() },
                rootChildWindowManager = rootChildWindowManager,
            )
        }
    }
}
