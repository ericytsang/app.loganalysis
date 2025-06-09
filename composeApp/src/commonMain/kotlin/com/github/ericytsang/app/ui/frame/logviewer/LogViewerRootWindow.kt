package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.util.usecase.BringWindowToFocusOnRequest
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
import com.github.ericytsang.app.ui.frame.workingfileseteditor.WorkingFileSetEditorViewModel
import com.github.ericytsang.app.ui.util.component.fillMaxBackground
import com.github.ericytsang.domain.objects.ConfigurationId
import com.github.ericytsang.kotlin.ImmutableCoroutineScope.Companion.asImmutableCoroutineScope

@Composable
fun LogViewerRootWindow(
    configurationId:ConfigurationId,
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
            LogViewerRoot(
                window = window,
                themeColors = themeColors,
                rootChildWindowManager = rootChildWindowManager,
                filterSetViewModelFactory = { filterType -> FilterTypeFilterSetViewModel(filterType, configurationId) },
                workingFileSetEditorViewModelFactory = { WorkingFileSetEditorViewModel.create(configurationId) },
                logViewerViewModelFactory = { LogViewerViewModel.create(configurationId) },
            )
        }
    }
}

