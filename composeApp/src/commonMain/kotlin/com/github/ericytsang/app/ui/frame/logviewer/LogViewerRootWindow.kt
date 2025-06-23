package com.github.ericytsang.app.ui.frame.logviewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import com.github.ericytsang.app.ui.util.usecase.BringWindowToFocusOnRequest
import com.github.ericytsang.app.ui.util.ChildWindowManager
import com.github.ericytsang.app.ui.util.ChildWindowManagerController
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
    val filterTypeFilterSetViewModelFactory = remember { FilterTypeFilterSetViewModelFactory(configurationId) }

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
                logFileListViewModelFactory = { LogFileListViewModelImpl(configurationId) },
                filterSetViewModelFactory = { filterType -> filterTypeFilterSetViewModelFactory.create(filterType) },
                workingFileSetEditorViewModelFactory = { WorkingFileSetEditorViewModel.create(configurationId) },
                logViewerViewModelFactory = { LogViewerViewModel.create(coroutineScope, configurationId) },
                reorderSidebarItemViewModelFactory = { ReorderSidebarItemViewModel.create(configurationId) },
                selectedItemsViewModelFactory = { SelectedItemsViewModelImpl() },
            )
        }
    }
}

