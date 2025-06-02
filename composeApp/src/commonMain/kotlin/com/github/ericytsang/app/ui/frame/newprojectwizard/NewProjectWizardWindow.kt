package com.github.ericytsang.app.ui.frame.newprojectwizard

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.util.usecase.BringWindowToFocusOnRequest
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
import com.github.ericytsang.app.ui.util.component.fillMaxBackground
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope

@Composable
fun NewProjectWizardWindow(
    rootChildWindowManager:ChildWindowManager,
    controller:ChildWindowManagerController,
)
{
    val coroutineScope = rememberCoroutineScope().asImmutableCoroutineScope()

    Window(
        onCloseRequest = { controller.removeSelf() },
        title = "New Project",
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
            NewProjectWizard(
                window = window,
                themeColors = themeColors,
                rootChildWindowManager = rootChildWindowManager,
                controller = controller,
            )
        }
    }
}