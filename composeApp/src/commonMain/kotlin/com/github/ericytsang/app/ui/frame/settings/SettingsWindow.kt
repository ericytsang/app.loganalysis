package com.github.ericytsang.app.ui.frame.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.frame.projectbrowser.BringWindowToFocusOnRequest
import com.github.ericytsang.app.ui.frame.workingfileseteditor.ChildWindowManagerController
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope

@Composable
fun SettingsWindow(
    controller: ChildWindowManagerController
)
{
    val coroutineScope = rememberCoroutineScope().asImmutableCoroutineScope()

    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "Settings",
    )
    {

        // bring the window to focus when requested by the child window manager
        BringWindowToFocusOnRequest(
            uiScope = coroutineScope,
            window = window,
            controller = controller,
        )

        // window content
        Settings()
    }
}
